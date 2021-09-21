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

package gg.solarmc.authplugin.auth;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.AuthState;
import gg.solarmc.authplugin.auth.handler.NextStageHandler;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.arim.omnibus.util.ThisClass;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

public final class Auth implements AuthState {

    private final String username;

    private final AtomicReference<NextStageHandler> currentHandler = new AtomicReference<>();

    private static final Logger logger = LoggerFactory.getLogger(ThisClass.get());

    public Auth(String username, NextStageHandler initialHandler) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(initialHandler, "initialHandler");

        this.username = username;
        this.currentHandler.set(initialHandler);
    }

    private interface HandlerAction {

        CompletionStage<NextStageHandler> handleEventWith(NextStageHandler sourceHandler);
    }

    private CompletionStage<?> handleEvent(Player player, HandlerAction action) {
        final NextStageHandler existingHandler;
        final TransitionalHandler transitionalHandler;
        {
            NextStageHandler mutableExistingHandler;
            TransitionalHandler mutableTransitionalHandler;
            do {
                mutableExistingHandler = currentHandler.get();
                mutableTransitionalHandler = new TransitionalHandler(mutableExistingHandler);
            } while (!currentHandler.compareAndSet(mutableExistingHandler, mutableTransitionalHandler));

            existingHandler = mutableExistingHandler;
            transitionalHandler = mutableTransitionalHandler;
        }
        return action.handleEventWith(existingHandler).thenAccept((nextHandler) -> {
            if (nextHandler == null) {
                throw new NullPointerException("Next handler must not be null for " + player);
            }
            if (nextHandler != existingHandler) {
                nextHandler.onTransition(player);
            }
            while (!currentHandler.compareAndSet(transitionalHandler, nextHandler)) {
                /*
                Try to update to the next handler. This loop can repeat if onCommand
                is called on the TransitionalHandler while the future is in progress
                 */
                Thread.onSpinWait();
            }
        });
    }

    private void checkPlayer(Player player) {
        if (!username.equals(player.getUsername())) {
            throw new IllegalArgumentException("Username of " + player + " must match " + username);
        }
    }

    public void onCommand(Player player, Command command) {
        checkPlayer(player);
        handleEvent(player, (existingHandler) -> existingHandler.onCommand(player, command))
                .exceptionally((ex) -> {
                    logger.error("Fatal error during command execution", ex);
                    return null;
                });
    }

    public CompletionStage<?> onLogin(LoginEvent loginEvent) {
        Player player = loginEvent.getPlayer();
        checkPlayer(player);
        return handleEvent(player, (existingHandler) -> existingHandler.onLogin(loginEvent))
                .exceptionally((ex) -> {
                    logger.error("Fatal error during login event handling", ex);
                    loginEvent.setResult(ResultedEvent.ComponentResult.denied(
                            Component.text("Internal error during authentication/login phase")));
                    return null;
                });
    }

    public void assertCurrentHandler(NextStageHandler expectedHandler) {
        assert currentHandler.get() == expectedHandler
                : "Expected " + expectedHandler + " but received " + currentHandler;
    }

    @Override
    public boolean isAuthenticated() {
        return currentHandler.get().isAuthenticated();
    }

}
