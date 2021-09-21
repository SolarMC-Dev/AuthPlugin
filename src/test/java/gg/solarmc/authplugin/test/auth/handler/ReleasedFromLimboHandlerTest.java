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

package gg.solarmc.authplugin.test.auth.handler;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import gg.solarmc.authplugin.auth.handler.ReleasedFromLimboHandler;
import gg.solarmc.authplugin.config.Config;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReleasedFromLimboHandlerTest {

    private final Config config;
    private final ProxyServer server;

    public ReleasedFromLimboHandlerTest(@Mock Config config, @Mock ProxyServer server) {
        this.config = config;
        this.server = server;
    }

    private ReleasedFromLimboHandler releasedFromLimboHandler;

    @BeforeEach
    public void setReleasedFromLimboHandler() {
        releasedFromLimboHandler = new ReleasedFromLimboHandler(config, server);
    }

    @Test
    public void readyToPlay(@Mock Player player, @Mock RegisteredServer releaseServer) {
        Component readyToPlay = Component.text("Ready to play");
        ConnectionRequestBuilder requestBuilder = mock(ConnectionRequestBuilder.class);
        {
            Config.Limbo limboConf = mock(Config.Limbo.class);
            when(config.limbo()).thenReturn(limboConf);
            when(limboConf.readyToPlay()).thenReturn(readyToPlay);
            when(limboConf.serverWhenReleasedFromLimbo()).thenReturn("release-server");
            when(server.getServer("release-server")).thenReturn(Optional.of(releaseServer));
            ConnectionRequestBuilder.Result result = mock(ConnectionRequestBuilder.Result.class);
            when(player.createConnectionRequest(releaseServer)).thenReturn(requestBuilder);
            when(requestBuilder.connect()).thenReturn(CompletableFuture.completedFuture(result));
            when(result.getStatus()).thenReturn(ConnectionRequestBuilder.Status.SUCCESS);
        }
        releasedFromLimboHandler.onTransition(player);
        verify(requestBuilder).connect();
        verify(player).sendMessage(readyToPlay);
    }
}
