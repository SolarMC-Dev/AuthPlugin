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

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import gg.solarmc.authplugin.auth.AuthProvider;
import gg.solarmc.authplugin.auth.Command;
import net.kyori.adventure.text.Component;

record CommandHandler(Command.Type type,
                      AuthProvider authProvider) implements RawCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("You are not a player"));
            return;
        }
        player.getAuthState(authProvider).onCommand(player, new Command(type, invocation.arguments()));
    }
}
