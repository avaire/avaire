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

package com.avairebot.plugin.releases;

import com.avairebot.contracts.plugin.PluginRelease;
import com.avairebot.plugin.PluginAsset;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GithubRelease implements PluginRelease {

    private final String name;
    private final String tag;
    private final String url;
    private final List<PluginAsset> assets;

    public GithubRelease(JSONObject obj) {
        name = obj.getString("name");
        tag = obj.getString("tag_name");
        url = obj.getString("html_url");

        assets = new ArrayList<>();
        for (Object o : obj.getJSONArray("assets")) {
            assets.add(new PluginAsset((JSONObject) o));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public List<PluginAsset> getAssets() {
        return assets;
    }
}
