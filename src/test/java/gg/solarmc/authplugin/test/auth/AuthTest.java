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

package gg.solarmc.authplugin.test.auth;

import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import gg.solarmc.authplugin.auth.Auth;
import gg.solarmc.authplugin.auth.Command;
import gg.solarmc.authplugin.auth.handler.NextStageHandler;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthTest {

    private final NextStageHandler initialHandler;
    private Auth auth;
    private final Player player;

    public AuthTest(@Mock NextStageHandler initialHandler, @Mock Player player) {
        this.initialHandler = initialHandler;
        this.player = player;
    }

    @BeforeEach
    public void setAuth() {
        String username = "A248";
        lenient().when(player.getUsername()).thenReturn(username);
        auth = new Auth(username, initialHandler);
    }

    @Test
    public void onCommandSameHandler() {
        when(initialHandler.onCommand(any(), any())).thenReturn(completedFuture(initialHandler));

        Command command = new Command(Command.Type.LOGIN, "arg");
        auth.onCommand(player, command);

        verify(initialHandler).onCommand(player, command);
        auth.assertCurrentHandler(initialHandler);
    }

    @Test
    public void onCommandNewHandler(@Mock NextStageHandler nextHandler) {
        when(initialHandler.onCommand(any(), any())).thenReturn(completedFuture(nextHandler));

        Command command = new Command(Command.Type.LOGIN, "arg");
        auth.onCommand(player, command);

        verify(initialHandler).onCommand(player, command);
        verify(nextHandler).onTransition(player);
        auth.assertCurrentHandler(nextHandler);
    }

    @Test
    public void onCommandRepeatedCommand(@Mock NextStageHandler nextHandler) {
        /*
        This test verifies behavior in the case that a player runs a command
        while an existing command is still executing.
         */

        CompletableFuture<NextStageHandler> futureNextHandler = new CompletableFuture<>();
        Component commandOutput = Component.text("Command output");
        when(initialHandler.onCommand(any(), any())).thenAnswer((i) -> {
            player.sendMessage(commandOutput);
            return futureNextHandler;
        });
        when(initialHandler.isAuthenticated()).thenReturn(true);

        Command command = new Command(Command.Type.LOGIN, "arg");

        // Player executes the command the first time
        auth.onCommand(player, command);
        verify(initialHandler).onCommand(player, command);
        verify(player).sendMessage(commandOutput);

        // Player executes another command while the first in progress
        auth.onCommand(player, command);
        //noinspection ConstantConditions
        verify(player).sendMessage(not(eq(commandOutput)));
        assertTrue(auth.isAuthenticated(), "Transitional handler should retain authentication status");

        // First command finishes
        futureNextHandler.complete(nextHandler);

        verify(nextHandler).onTransition(player);
        auth.assertCurrentHandler(nextHandler);
    }

    @Test
    public void onLoginSameHandler() {
        when(initialHandler.onLogin(any())).thenReturn(completedFuture(initialHandler));

        LoginEvent loginEvent = new LoginEvent(player);
        auth.onLogin(loginEvent).toCompletableFuture().join();

        verify(initialHandler).onLogin(loginEvent);
        auth.assertCurrentHandler(initialHandler);
    }

    @Test
    public void onLoginNewHandler(@Mock NextStageHandler nextHandler) {
        when(initialHandler.onLogin(any())).thenReturn(completedFuture(nextHandler));

        LoginEvent loginEvent = new LoginEvent(player);
        auth.onLogin(loginEvent).toCompletableFuture().join();

        verify(initialHandler).onLogin(loginEvent);
        verify(nextHandler).onTransition(player);
        auth.assertCurrentHandler(nextHandler);
    }

    @Test
    public void onLoginException(@Mock NextStageHandler nextHandler) {
        when(initialHandler.onLogin(any())).thenReturn(CompletableFuture.failedFuture(new StacklessException()));

        LoginEvent loginEvent = new LoginEvent(player);
        auth.onLogin(loginEvent).toCompletableFuture().join();

        verify(initialHandler).onLogin(loginEvent);
        assertFalse(loginEvent.getResult().isAllowed(), "Login event should be disallowed in case of critical failure");
    }

    static final class StacklessException extends RuntimeException {

        StacklessException() {
            super("No stack trace available", null, true, false);
        }
    }

    @Test
    public void onCommandWrongPlayer(@Mock Player otherPlayer) {
        when(otherPlayer.getUsername()).thenReturn("OtherName");
        Command command = new Command(Command.Type.LOGIN, "pass");
        assertThrows(RuntimeException.class, () -> auth.onCommand(otherPlayer, command));
    }

    @Test
    public void onLoginWrongPlayer(@Mock Player otherPlayer) {
        when(otherPlayer.getUsername()).thenReturn("OtherName");
        LoginEvent loginEvent = new LoginEvent(otherPlayer);
        assertThrows(RuntimeException.class, () -> auth.onLogin(loginEvent).toCompletableFuture().join());
    }

}
