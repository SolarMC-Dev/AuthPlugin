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

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.AuthenticationProvider;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.authentication.UserWithDataNotYetLoaded;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public final class DataFulfillment {

    private final AuthenticationProvider.DataLoadController dataLoadController;

    @Inject
    public DataFulfillment(AuthenticationProvider.DataLoadController dataLoadController) {
        this.dataLoadController = dataLoadController;
    }

    public UserWithDataNotYetLoaded createUser(Player player) {
        return new PlayerAsUser(player);
    }

    private final class PlayerAsUser implements UserWithDataNotYetLoaded {

        private final Player player;

        private PlayerAsUser(Player player) {
            this.player = player;
        }

        @Override
        public UUID mcUuid() {
            return player.getUniqueId();
        }

        @Override
        public String mcUsername() {
            return player.getUsername();
        }

        @Override
        public void loadData(Transaction transaction, int userId) {
            dataLoadController.addSolarPlayer(player, transaction, userId);
        }
    }
}
