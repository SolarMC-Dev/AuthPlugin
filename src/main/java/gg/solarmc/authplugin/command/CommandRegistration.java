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

package gg.solarmc.authplugin.command;

import com.velocitypowered.api.proxy.ProxyServer;
import gg.solarmc.authplugin.HasLifecycle;
import gg.solarmc.authplugin.auth.AuthProvider;
import gg.solarmc.authplugin.auth.Command;

import jakarta.inject.Inject;

public final class CommandRegistration implements HasLifecycle {

    private final AuthProvider authProvider;
    private final ProxyServer server;

    @Inject
    public CommandRegistration(AuthProvider authProvider, ProxyServer server) {
        this.authProvider = authProvider;
        this.server = server;
    }

    @Override
    public void start() {
        var commandManager = server.getCommandManager();
        for (Command.Type type : Command.Type.values()) {
            commandManager.register(
                    commandManager.metaBuilder(type.toString()).build(),
                    new CommandHandler(type, authProvider));
        }
    }

    @Override
    public void stop() {
        for (Command.Type type : Command.Type.values()) {
            server.getCommandManager().unregister(type.toString());
        }
    }
}
