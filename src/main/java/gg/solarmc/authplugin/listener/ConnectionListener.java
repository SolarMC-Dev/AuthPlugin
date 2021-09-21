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

package gg.solarmc.authplugin.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import gg.solarmc.authplugin.auth.Auth;
import gg.solarmc.authplugin.auth.AuthProvider;
import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.authentication.AuthenticationCenter;

import jakarta.inject.Inject;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.arim.omnibus.util.ThisClass;

public final class ConnectionListener {

    private final AuthProvider authProvider;
    private final DataCenter dataCenter;
    private final AuthenticationCenter authCenter;
    private final ConnectionHandling connectionHandling;

    private static final Logger logger = LoggerFactory.getLogger(ThisClass.get());

    @Inject
    public ConnectionListener(AuthProvider authProvider, DataCenter dataCenter,
                              AuthenticationCenter authCenter, ConnectionHandling connectionHandling) {
        this.authProvider = authProvider;
        this.dataCenter = dataCenter;
        this.authCenter = authCenter;
        this.connectionHandling = connectionHandling;
    }

    @Subscribe
    public EventTask onPreLogin(PreLoginEvent event) {
        String username = event.getUsername();
        return EventTask.resumeWhenComplete(dataCenter.transact((tx) -> {
            return authCenter.findExistingUserWithName(tx, username);
        }).thenCompose((autoLoginResult) -> {
            return connectionHandling.handleEvent(event, autoLoginResult);
        }).exceptionally((ex) -> {
            logger.error("Fatal error during pre-login handling", ex);
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text("Internal error during authentication/pre-login phase")));
            return null;
        }));
    }

    @Subscribe
    public EventTask onLogin(LoginEvent event) {
        Auth auth = event.getPlayer().getAuthState(authProvider);
        return EventTask.resumeWhenComplete(auth.onLogin(event).toCompletableFuture());
    }

}
