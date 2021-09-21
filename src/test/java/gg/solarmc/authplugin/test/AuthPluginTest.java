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

package gg.solarmc.authplugin.test;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.AuthenticationProvider.DataLoadController;
import gg.solarmc.authplugin.AuthPlugin;
import gg.solarmc.loader.DataCenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthPluginTest {

    @Test
    public void load(@Mock ProxyServer server, @TempDir Path folder, @Mock PluginContainer plugin) {
        AuthPlugin authPlugin = new AuthPlugin();
        assertDoesNotThrow(() -> authPlugin.load(server, folder, plugin));
    }

    @Test
    public void delayedInit(@Mock ProxyServer server, @TempDir Path folder, @Mock PluginContainer plugin,
                            @Mock DataCenter dataCenter, @Mock DataLoadController dataLoadController) {
        when(server.getDataCenter()).thenReturn(dataCenter);
        AuthPlugin authPlugin = new AuthPlugin();
        authPlugin.load(server, folder, plugin);
        assertDoesNotThrow(() -> authPlugin.delayedInit(dataLoadController));
    }
}
