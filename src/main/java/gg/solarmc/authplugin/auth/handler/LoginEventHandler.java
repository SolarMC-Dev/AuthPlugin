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

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import gg.solarmc.authplugin.auth.Command;
import gg.solarmc.authplugin.auth.DataFulfillment;
import gg.solarmc.authplugin.config.Config;
import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.authentication.AuthenticationCenter;
import gg.solarmc.loader.authentication.VerifiablePassword;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.concurrent.CompletionStage;

@Singleton
public final class LoginEventHandler implements NextStageHandler {

    private final Config config;
    private final DataCenter dataCenter;
    private final AuthenticationCenter authCenter;
    private final DataFulfillment dataFulfiller;

    private final PremiumWillSetYouFreeHandler premiumWillSetYouFreeHandler;
    private final EnterPasswordHandlerFactory enterPasswordHandlerFactory;
    private final CreateAccountHandler createAccountHandler;
    private final DisconnectedHandler disconnectedHandler;

    @Inject
    public LoginEventHandler(Config config, DataCenter dataCenter,
                             AuthenticationCenter authCenter, DataFulfillment dataFulfiller,
                             PremiumWillSetYouFreeHandler premiumWillSetYouFreeHandler, EnterPasswordHandlerFactory enterPasswordHandlerFactory,
                             CreateAccountHandler createAccountHandler, DisconnectedHandler disconnectedHandler) {
        this.config = config;
        this.dataCenter = dataCenter;
        this.authCenter = authCenter;
        this.dataFulfiller = dataFulfiller;
        this.premiumWillSetYouFreeHandler = premiumWillSetYouFreeHandler;
        this.enterPasswordHandlerFactory = enterPasswordHandlerFactory;
        this.createAccountHandler = createAccountHandler;
        this.disconnectedHandler = disconnectedHandler;
    }

    @Override
    public CompletionStage<NextStageHandler> onCommand(Player player, Command command) {
        throw new IllegalStateException("Cannot invoke commands before LoginEvent");
    }

    @Override
    public CompletionStage<NextStageHandler> onLogin(LoginEvent loginEvent) {
        return dataCenter.transact((tx) -> {
            return authCenter.attemptLoginOfIdentifiedUser(tx, dataFulfiller.createUser(loginEvent.getPlayer()));
        }).thenApply((loginAttempt) -> {
            return switch (loginAttempt.resultType()) {
            case PREMIUM_PERMITTED -> premiumWillSetYouFreeHandler;
            case NEEDS_PASSWORD -> {
                VerifiablePassword password = loginAttempt.verifiablePassword();
                yield enterPasswordHandlerFactory.createHandler(password);
            }
            case NEEDS_ACCOUNT -> createAccountHandler;
            case DENIED_PREMIUM_TOOK_NAME -> {
                loginEvent.setResult(ResultedEvent.ComponentResult.denied(
                        config.logons().deniedPremiumTookName()));
                yield disconnectedHandler;
            }
            case DENIED_CASE_SENSITIVITY_OF_NAME -> {
                loginEvent.setResult(ResultedEvent.ComponentResult.denied(
                        config.logons().deniedCaseSensitivityOfName()));
                yield disconnectedHandler;
            }
            };
        });
    }

    @Override
    public void onTransition(Player player) {

    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }
}
