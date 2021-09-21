/*
 * authplugin
 * Copyright © 2021 SolarMC Developers
 *
 * authplugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * authplugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with authplugin. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Affero General Public License.
 */

package gg.solarmc.authplugin.auth.handler;

import com.velocitypowered.api.proxy.Player;
import gg.solarmc.authplugin.auth.Command;
import gg.solarmc.authplugin.auth.DataFulfillment;
import gg.solarmc.authplugin.config.Config;
import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.authentication.AuthenticationCenter;
import gg.solarmc.loader.authentication.VerifiablePassword;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public final class EnterPasswordHandlerFactory {

    private final Config config;
    private final DataCenter dataCenter;
    private final AuthenticationCenter authCenter;
    private final DataFulfillment dataFulfillment;
    private final ReleasedFromLimboHandler releasedFromLimboHandler;
    private final DisconnectedHandler disconnectedHandler;

    @Inject
    public EnterPasswordHandlerFactory(Config config, DataCenter dataCenter,
                                       AuthenticationCenter authCenter, DataFulfillment dataFulfillment,
                                       ReleasedFromLimboHandler releasedFromLimboHandler,
                                       DisconnectedHandler disconnectedHandler) {
        this.config = config;
        this.dataCenter = dataCenter;
        this.authCenter = authCenter;
        this.dataFulfillment = dataFulfillment;
        this.releasedFromLimboHandler = releasedFromLimboHandler;
        this.disconnectedHandler = disconnectedHandler;
    }

    public NextStageHandler createHandler(VerifiablePassword requiredPassword) {
        return new Handler(requiredPassword);
    }

    private final class Handler implements NextStageHandler {

        private final VerifiablePassword requiredPassword;

        private Handler(VerifiablePassword requiredPassword) {
            this.requiredPassword = requiredPassword;
        }

        @Override
        public CompletionStage<NextStageHandler> onCommand(Player player, Command command) {
            return switch (command.type()) {
                case REGISTER -> {
                    player.sendMessage(config.commandNotAtThisTime());
                    yield CompletableFuture.completedFuture(this);
                }
                case LOGIN -> CompletableFuture.supplyAsync(() -> {
                    // Hash password
                    return authCenter.hashPassword(
                            command.argument(), requiredPassword.passwordSalt(), requiredPassword.instructions());
                }).thenCompose((password) -> {
                    // Verify password
                    if (!requiredPassword.matches(password)) {
                        // Incorrect password
                        player.sendMessage(config.accountLogin().incorrectPassword());
                        return CompletableFuture.completedFuture(this);
                    }
                    // Correct password
                    return dataCenter.transact((tx) -> {
                        return authCenter.completeLoginAndPossiblyMigrate(tx, dataFulfillment.createUser(player));
                    }).thenApply((completeLoginResult) -> {
                        return switch (completeLoginResult) {
                        case USER_ID_MISSING:
                            player.disconnect(config.accountLogin().userIdMissing());
                            yield disconnectedHandler;
                        case MIGRATED_TO_PREMIUM:
                            player.sendMessage(config.accountLogin().migrated());
                            // Fall-through
                        case NORMAL:
                            player.sendMessage(config.accountLogin().correctPassword());
                            yield releasedFromLimboHandler;
                        };
                    });
                });
            };
        }

        @Override
        public void onTransition(Player player) {
            player.sendMessage(config.accountLogin().shouldLogin());
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

    }

}
