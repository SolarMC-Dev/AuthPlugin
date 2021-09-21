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
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import gg.solarmc.authplugin.auth.Command;
import gg.solarmc.authplugin.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.arim.omnibus.util.ThisClass;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public final class ReleasedFromLimboHandler implements NextStageHandler {

    private final Config config;
    private final ProxyServer server;

    private static final Logger logger = LoggerFactory.getLogger(ThisClass.get());

    @Inject
    public ReleasedFromLimboHandler(Config config, ProxyServer server) {
        this.config = config;
        this.server = server;
    }

    @Override
    public CompletionStage<NextStageHandler> onCommand(Player player, Command command) {
        player.sendMessage(config.commandNotAtThisTime());
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public void onTransition(Player player) {
        var limboConf = config.limbo();
        Optional<RegisteredServer> optServer = server.getServer(limboConf.serverWhenReleasedFromLimbo());
        if (optServer.isEmpty()) {
            logger.warn("Limbo release server {} does not exist. As such, player {} will not move to it",
                    limboConf.serverWhenReleasedFromLimbo(), player);
            return;
        }
        RegisteredServer serverToMoveTo = optServer.get();
        player.createConnectionRequest(serverToMoveTo).connect().thenAccept((connectResult) -> {
            switch (connectResult.getStatus()) {
            case SUCCESS -> {
                player.sendMessage(limboConf.readyToPlay());
            }
            case ALREADY_CONNECTED, CONNECTION_IN_PROGRESS -> {
                throw new IllegalStateException(
                        "Player " + player + " is already connected or connecting to " +
                                "limbo release server " + serverToMoveTo + ". Result is " + connectResult);
            }
            case CONNECTION_CANCELLED, SERVER_DISCONNECTED -> {
                logger.warn("Unable to connect {} to limbo release server {}; result is {}",
                        player, serverToMoveTo, connectResult);
                player.sendMessage(limboConf.unableToConnectToReleaseServer());
            }
            }
        }).exceptionally((ex) -> {
            logger.warn("Exception while moving player to server-when-released-from-limbo", ex);
            return null;
        });
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
