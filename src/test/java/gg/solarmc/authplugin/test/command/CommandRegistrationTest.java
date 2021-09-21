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

package gg.solarmc.authplugin.test.command;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.solarmc.authplugin.auth.Auth;
import gg.solarmc.authplugin.auth.AuthProvider;
import gg.solarmc.authplugin.auth.Command;
import gg.solarmc.authplugin.command.CommandRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandRegistrationTest {

    private final AuthProvider authProvider;
    private final ProxyServer server;

    private CommandRegistration commandRegistration;

    public CommandRegistrationTest(@Mock AuthProvider authProvider, @Mock ProxyServer server) {
        this.authProvider = authProvider;
        this.server = server;
    }

    @BeforeEach
    public void setCommandRegistration() {
        commandRegistration = new CommandRegistration(authProvider, server);
    }

    @Test
    public void registerAndExecuteCommand(@Mock Player player,
                                          @Mock Auth auth,
                                          @Mock CommandManager commandManager) {
        when(player.getAuthState(authProvider)).thenReturn(auth);
        when(server.getCommandManager()).thenReturn(commandManager);
        Meta meta = new Meta("login");
        when(commandManager.metaBuilder("login")).thenReturn(meta.builder());
        when(commandManager.metaBuilder(not(eq("login")))).thenReturn(new Meta("othercmd").builder());

        ArgumentCaptor<RawCommand> commandCaptor = ArgumentCaptor.forClass(RawCommand.class);
        commandRegistration.start();
        verify(commandManager).register(eq(meta), commandCaptor.capture());

        RawCommand command = commandCaptor.getValue();
        command.execute(new Invocation("login", player, "pass"));
        verify(auth).onCommand(player, new Command(Command.Type.LOGIN, "pass"));
    }
}
