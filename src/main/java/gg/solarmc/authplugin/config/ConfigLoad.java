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

package gg.solarmc.authplugin.config;

import space.arim.api.jsonchat.adventure.ChatMessageComponentSerializer;
import space.arim.api.util.dazzleconf.ChatMessageSerializer;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.helper.ConfigurationHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public record ConfigLoad(Path dataFolder) {

    public Config createConfig() {
        var configHelper = new ConfigurationHelper<>(
                dataFolder,
                "createConfig.yml",
                SnakeYamlConfigurationFactory.create(Config.class, getOptions()));
        try {
            return configHelper.reloadConfigData();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (InvalidConfigException ex) {
            throw new RuntimeException("Broken configuration. Fix it and restart", ex);
        }
    }

    private ConfigurationOptions getOptions() {
        return new ConfigurationOptions.Builder()
                .addSerialiser(new ChatMessageSerializer(new ChatMessageComponentSerializer()))
                .build();
    }
}
