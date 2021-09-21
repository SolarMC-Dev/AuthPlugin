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

package gg.solarmc.authplugin.test.listener;

import gg.solarmc.authplugin.listener.ListenerRegistration;
import org.junit.jupiter.api.Test;
import space.arim.api.util.testing.InjectableConstructor;

public class ListenerRegistrationTest {

    @Test
    public void allDeclared() {
        new InjectableConstructor(ListenerRegistration.class)
                .verifyParametersContainSubclassesOf(Object.class, (cls) -> {
                    return cls.getName().endsWith("Listener") && cls.getPackageName().endsWith("listener");
                });
    }
}
