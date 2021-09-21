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

package gg.solarmc.authplugin;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.player.AuthenticationProvider;

public final class AuthProviderFactory implements AuthenticationProvider.ProviderFactory {

    @Override
    public AuthenticationProvider<?> createProvider(PluginManager pluginManager,
                                                    AuthenticationProvider.DataLoadController dataLoadController) {
        AuthPlugin authPlugin = (AuthPlugin) pluginManager
                .getPlugin(AuthPlugin.class.getModule().getName())
                .flatMap(PluginContainer::getInstance)
                .orElseThrow(() -> new IllegalStateException("Own plugin not found"));
        return authPlugin.delayedInit(dataLoadController);
    }

}
