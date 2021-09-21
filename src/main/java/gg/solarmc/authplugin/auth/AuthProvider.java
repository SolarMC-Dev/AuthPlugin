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

import com.velocitypowered.api.proxy.player.AuthenticationProvider;
import gg.solarmc.authplugin.auth.handler.NextStageHandler;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public final class AuthProvider implements AuthenticationProvider<Auth> {

    private final NextStageHandler initialHandler;

    @Inject
    public AuthProvider(@Named("initialHandler") NextStageHandler initialHandler) {
        this.initialHandler = initialHandler;
    }

    @Override
    public Auth createAuthState(String username) {
        return new Auth(username, initialHandler);
    }

}
