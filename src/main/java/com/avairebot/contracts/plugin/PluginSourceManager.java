/*
 * Copyright (c) 2019.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.contracts.plugin;

import com.avairebot.plugin.PluginRepository;
import com.avairebot.plugin.PluginSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface PluginSourceManager {

    Cache<String, List<PluginRelease>> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build();

    /**
     * Gets the plugin source used for the current source manager.
     *
     * @return The plugin source used for the current source manager.
     */
    PluginSource getPluginSource();

    /**
     * Gets the URL of the latest downloadable JAR file from the given plugin, if the
     * plugin doesn't have any released JAR files, null will be returned instead.
     *
     * @param repository The plugin repository that the latest source URl should
     * @return The URL to the latest downloadable JAR file of the plugin, or {@code NULL}
     *         if the plugin doesn't have any JAr files released.
     */
    @Nullable
    String getLatestDownloadableJarFile(PluginRepository repository);

    /**
     * Gets a list of all the plugin releases for the given plugin
     * repository using the plugin source manager.
     *
     * @param repository The plugin repository that the releases should be pulled from.
     * @return A list of plugin releases for the given plugin repository,
     *         or an empty list if the plugin repo has no no releases.
     */
    List<PluginRelease> getPluginReleases(PluginRepository repository);

    /**
     * Gets the cache key value from the given plugin repository.
     *
     * @param pluginRepository The plugin repository the key should be generated for.
     * @return The generated plugin repository cache key.
     */
    default String asKey(PluginRepository pluginRepository) {
        return pluginRepository.getSource().getName() + "-" + pluginRepository.getRepository();
    }
}
