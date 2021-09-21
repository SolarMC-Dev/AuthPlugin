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

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.solarmc.authplugin.HasLifecycle;

import jakarta.inject.Inject;
import java.util.List;

public final class ListenerRegistration implements HasLifecycle {

    private final ProxyServer server;
    private final PluginContainer plugin;
    private final List<Object> listeners;

    public ListenerRegistration(ProxyServer server, PluginContainer plugin, List<Object> listeners) {
        this.server = server;
        this.plugin = plugin;
        this.listeners = List.copyOf(listeners);
    }

    @Inject
    public ListenerRegistration(ProxyServer server, PluginContainer plugin,
                                ConnectionListener connectionListener, ServerConnectListener serverConnectListener) {
        this(server, plugin, List.of(connectionListener, serverConnectListener));
    }

    @Override
    public void start() {
        for (Object listener : listeners) {
            server.getEventManager().register(plugin, listener);
        }
    }

    @Override
    public void stop() {
        for (Object listener : listeners) {
            server.getEventManager().unregisterListener(plugin, listener);
        }
    }
}
