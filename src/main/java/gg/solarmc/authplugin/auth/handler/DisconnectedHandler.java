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

import jakarta.inject.Singleton;
import java.util.concurrent.CompletionStage;

@Singleton
public final class DisconnectedHandler implements NextStageHandler {

    @Override
    public CompletionStage<NextStageHandler> onCommand(Player player, Command command) {
        throw new UnsupportedOperationException("Impossible command execution");
    }

    @Override
    public void onTransition(Player player) {

    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }
}
