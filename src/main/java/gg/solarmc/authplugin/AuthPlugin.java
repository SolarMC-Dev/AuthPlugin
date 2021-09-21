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

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.LaunchablePlugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.AuthenticationProvider;
import com.velocitypowered.api.proxy.player.AuthenticationProvider.DataLoadController;
import gg.solarmc.authplugin.auth.AuthProvider;
import space.arim.injector.Injector;
import space.arim.injector.InjectorBuilder;

import java.nio.file.Path;

public final class AuthPlugin implements LaunchablePlugin {

    private InjectorBuilder injectorBuilder;
    private Lifecycle lifecycle;

    @Override
    public void load(ProxyServer server, Path dataFolder, PluginContainer container) {
        injectorBuilder = new InjectorBuilder()
                .bindInstance(ProxyServer.class, server)
                .bindInstance(Path.class, dataFolder)
                .bindInstance(PluginContainer.class, container);
    }

    public AuthenticationProvider<?> delayedInit(DataLoadController dataLoadController) {
        Injector injector = injectorBuilder
                .bindInstance(DataLoadController.class, dataLoadController)
                .addBindModules(new Binder())
                .build();
        lifecycle = injector.request(Lifecycle.class);
        return injector.request(AuthProvider.class);
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent initializeEvent) {
        lifecycle.start();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent shutdownEvent) {
        lifecycle.close();
    }

}
