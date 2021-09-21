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

package gg.solarmc.authplugin.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import gg.solarmc.authplugin.auth.AuthProvider;
import gg.solarmc.authplugin.auth.Auth;
import gg.solarmc.authplugin.config.Config;
import jakarta.inject.Inject;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public final class ServerConnectListener {

    private final AuthProvider authProvider;
    private final ProxyServer server;
    private final Config config;

    @Inject
    public ServerConnectListener(AuthProvider authProvider, ProxyServer server, Config config) {
        this.authProvider = authProvider;
        this.server = server;
        this.config = config;
    }

    @Subscribe
    public void onInitialServer(PlayerChooseInitialServerEvent event) {
        Auth auth = event.getPlayer().getAuthState(authProvider);
        if (auth.isAuthenticated()) {
            return;
        }
        Optional<RegisteredServer> limboServer = server.getServer(config.limbo().serverWhileInLimbo());
        if (limboServer.isEmpty()) {
            event.getPlayer().disconnect(Component.text("Internal issue: No such limbo server found"));
            return;
        }
        event.setInitialServer(limboServer.get());
    }

    @Subscribe
    public void onServerSwitch(ServerPreConnectEvent event) {
        ServerPreConnectEvent.ServerResult result = event.getResult();
        if (!result.isAllowed()) {
            return;
        }
        Auth auth = event.getPlayer().getAuthState(authProvider);
        if (auth.isAuthenticated()) {
            return;
        }
        Optional<RegisteredServer> targetServer = result.getServer();
        if (targetServer.isPresent() &&
                targetServer.get().getServerInfo().getName().equalsIgnoreCase(config.limbo().serverWhileInLimbo())) {
            return;
        }
        event.setResult(ServerPreConnectEvent.ServerResult.denied());
    }
}
