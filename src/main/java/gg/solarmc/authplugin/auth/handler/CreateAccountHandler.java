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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public final class CreateAccountHandler implements NextStageHandler {

    private final Config config;
    private final DataCenter dataCenter;
    private final AuthenticationCenter authCenter;
    private final DataFulfillment dataFulfillment;
    private final ReleasedFromLimboHandler releasedFromLimboHandler;
    private final DisconnectedHandler disconnectedHandler;

    @Inject
    public CreateAccountHandler(Config config, DataCenter dataCenter,
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

    @Override
    public CompletionStage<NextStageHandler> onCommand(Player player, Command command) {
        return switch (command.type()) {
        case LOGIN -> {
            player.sendMessage(config.commandNotAtThisTime());
            yield CompletableFuture.completedFuture(this);
        }
        case REGISTER -> CompletableFuture.supplyAsync(() -> {
            // Hash new password
            return authCenter.hashNewPassword(command.argument());
        }).thenCompose((password) -> dataCenter.transact((tx) -> {
            // Attempt to create account
            return authCenter.createAccount(tx, dataFulfillment.createUser(player), password);
        })).thenApply((createAccountResult) -> {
            // Check if account creation is successful
            return switch (createAccountResult) {
            case CREATED -> {
                player.sendMessage(config.accountCreation().created());
                yield releasedFromLimboHandler;
            }
            case CONFLICT -> {
                player.disconnect(config.accountCreation().conflicting());
                yield disconnectedHandler;
            }
            };
        });
        };
    }

    @Override
    public void onTransition(Player player) {
        player.sendMessage(config.accountCreation().shouldCreate());
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }
}
