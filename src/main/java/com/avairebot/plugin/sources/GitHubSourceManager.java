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

package com.avairebot.plugin.sources;

import com.avairebot.contracts.plugin.PluginAsset;
import com.avairebot.contracts.plugin.PluginRelease;
import com.avairebot.contracts.plugin.PluginSourceManager;
import com.avairebot.plugin.PluginRepository;
import com.avairebot.plugin.PluginSource;
import com.avairebot.plugin.releases.GithubRelease;
import com.avairebot.utilities.CacheUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHubSourceManager implements PluginSourceManager {

    @Override
    public PluginSource getPluginSource() {
        return PluginSource.GITHUB;
    }

    @Nullable
    @Override
    public String getLatestDownloadableJarFile(PluginRepository repository) {
        List<PluginRelease> releases = getPluginReleases(repository);

        if (releases.isEmpty()) {
            return null;
        }

        for (PluginAsset asset : releases.get(0).getAssets()) {
            if (asset.getName().endsWith(".jar")) {
                return asset.getDownloadableUrl();
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PluginRelease> getPluginReleases(PluginRepository repository) {
        return (List<PluginRelease>) CacheUtil.getUncheckedUnwrapped(cache, asKey(repository), () -> {
            try {
                Connection.Response response = Jsoup.connect(getPluginSource().getReleasesUrl(repository.getRepository()))
                    .ignoreContentType(true)
                    .execute();

                List<PluginRelease> releases = new ArrayList<>();
                for (Object obj : new JSONArray(response.body())) {
                    releases.add(new GithubRelease((JSONObject) obj));
                }

                return releases;
            } catch (IOException e) {
                return new ArrayList<>();
            }
        });
    }
}
