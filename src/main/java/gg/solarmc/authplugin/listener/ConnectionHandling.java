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

import com.velocitypowered.api.event.connection.PreLoginEvent;
import gg.solarmc.authplugin.config.Config;
import gg.solarmc.loader.authentication.AutoLoginResult;
import jakarta.inject.Inject;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.arim.api.util.web.RemoteNameUUIDApi;
import space.arim.omnibus.util.ThisClass;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult.forceOfflineMode;
import static com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult.forceOnlineMode;
import static java.util.concurrent.CompletableFuture.completedFuture;

public final class ConnectionHandling {

    private final Config config;
    private final RemoteNameUUIDApi httpMojangApi;

    private static final Logger logger = LoggerFactory.getLogger(ThisClass.get());

    @Inject
    public ConnectionHandling(Config config, RemoteNameUUIDApi httpMojangApi) {
        this.config = config;
        this.httpMojangApi = httpMojangApi;
    }

    private CompletableFuture<Boolean> isPremiumPerMojangApi(String username) {
        return httpMojangApi.lookupUUID(username).thenApply((remoteApiResult) -> {
            return switch (remoteApiResult.getResultType()) {
                case FOUND -> true;
                case NOT_FOUND -> false;
                case RATE_LIMITED -> {
                    logger.warn("Rate-limited by the Mojang API");
                    yield config.logons().assumePremiumIfRateLimited();
                }
                case ERROR -> throw new CompletionException(remoteApiResult.getException());
                default /* case UNKNOWN */ -> throw new IllegalStateException("Deprecated result type used");
            };
        });
    }

    CompletableFuture<PreLoginEvent.PreLoginComponentResult> handleEvent(PreLoginEvent event,
                                                                         AutoLoginResult autoLoginResult) {
        return switch (autoLoginResult.resultType()) {

            case PREMIUM, CRACKED_BUT_DESIRES_MIGRATION -> {
                yield completedFuture(forceOnlineMode());
            }
            case CRACKED -> {
                yield completedFuture(forceOfflineMode());
            }
            case NONE_FOUND -> {
                yield isPremiumPerMojangApi(event.getUsername())
                        .thenApply((premium) -> (premium) ? forceOnlineMode() : forceOfflineMode());
            }
            case DENIED_CASE_SENSITIVITY_OF_NAME -> {
                Component message = config.logons().deniedCaseSensitivityOfName();
                yield completedFuture(PreLoginEvent.PreLoginComponentResult.denied(message));
            }
        };
    }

}
