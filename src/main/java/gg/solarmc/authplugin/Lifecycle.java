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

import gg.solarmc.authplugin.command.CommandRegistration;
import gg.solarmc.authplugin.listener.ListenerRegistration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public final class Lifecycle implements AutoCloseable {

    private final List<HasLifecycle> hasLifecycles;

    public Lifecycle(List<HasLifecycle> hasLifecycles) {
        this.hasLifecycles = hasLifecycles;
    }

    @Inject
    public Lifecycle(ListenerRegistration listenerRegistration, CommandRegistration commandRegistration) {
        this(List.of(listenerRegistration, commandRegistration));
    }

    public void start() {
        hasLifecycles.forEach(HasLifecycle::start);
    }

    @Override
    public void close() {
        hasLifecycles.forEach(HasLifecycle::stop);
    }
}
