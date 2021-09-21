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

package gg.solarmc.authplugin.test.command;

import com.mojang.brigadier.tree.CommandNode;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;

import java.util.Collection;
import java.util.Set;

record Meta(Collection<String> getAliases, Collection<CommandNode<CommandSource>> getHints)
        implements CommandMeta {

    Meta(String alias) {
        this(Set.of(alias), Set.of());
    }

    CommandMeta.Builder builder() {
        return new Builder() {
            @Override
            public Builder aliases(String... aliases) {
                return this;
            }

            @Override
            public Builder hint(CommandNode<CommandSource> node) {
                return this;
            }

            @Override
            public CommandMeta build() {
                return Meta.this;
            }
        };
    }
}
