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

import com.velocitypowered.api.proxy.ProxyServer;
import gg.solarmc.authplugin.auth.handler.LoginEventHandler;
import gg.solarmc.authplugin.auth.handler.NextStageHandler;
import gg.solarmc.authplugin.config.Config;
import gg.solarmc.authplugin.config.ConfigLoad;
import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.authentication.AuthenticationCenter;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import space.arim.api.util.web.HttpMojangApi;
import space.arim.api.util.web.RemoteNameUUIDApi;

import java.nio.file.Path;

public final class Binder {

    @Singleton
    public AuthenticationCenter authenticationCenter() {
        return AuthenticationCenter.create();
    }

    public @Named("initialHandler") NextStageHandler initialHandler(LoginEventHandler loginEventHandler) {
        return loginEventHandler;
    }

    @Singleton
    public Config config(Path dataFolder) {
        return new ConfigLoad(dataFolder).createConfig();
    }

    @Singleton
    public RemoteNameUUIDApi httpMojangApi() {
        return HttpMojangApi.create();
    }

    public DataCenter dataCenter(ProxyServer server) {
        return server.getDataCenter();
    }

}
