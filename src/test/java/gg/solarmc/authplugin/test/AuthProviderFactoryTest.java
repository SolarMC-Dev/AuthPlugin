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
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.player.AuthState;
import com.velocitypowered.api.proxy.player.AuthenticationProvider;
import com.velocitypowered.api.proxy.player.AuthenticationProvider.ProviderFactory;
import gg.solarmc.authplugin.AuthPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthProviderFactoryTest {

    private ServiceLoader<ProviderFactory> serviceLoader() {
        return ServiceLoader.load(getClass().getModule().getLayer(), ProviderFactory.class);
    }

    @Test
    public void serviceLoad() {
        var optProviderFactory = assertDoesNotThrow(() -> serviceLoader().findFirst());
        assertNotNull(optProviderFactory.orElse(null));
    }

    @Test
    public void createProvider(@Mock PluginManager pluginManager,
                               @Mock AuthenticationProvider.DataLoadController dataLoadController,
                               @Mock AuthPlugin authPlugin, @Mock PluginContainer authPluginContainer,
                               @Mock AuthenticationProvider<AuthState> authProvider) {
        when(pluginManager.getPlugin("gg.solarmc.authplugin")).thenReturn(Optional.of(authPluginContainer));
        //noinspection unchecked
        when((Optional<AuthPlugin>) authPluginContainer.getInstance()).thenReturn(Optional.of(authPlugin));
        //noinspection unchecked
        when((AuthenticationProvider<AuthState>) authPlugin.delayedInit(any())).thenReturn(authProvider);

        ProviderFactory providerFactory = serviceLoader().findFirst().orElseThrow();
        AuthenticationProvider<?> provider = assertDoesNotThrow(
                () -> providerFactory.createProvider(pluginManager, dataLoadController));
        assertNotNull(provider);

        verify(authPlugin).delayedInit(dataLoadController);
    }
}
