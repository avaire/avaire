/*
 * Copyright (c) 2018.
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

package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.cache.CacheType;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.music.PlayCommand;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.utilities.RandomUtil;
import com.google.gson.JsonElement;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;

public class RequestSong extends Intent {

    private static final Map<String, List<String>> categories = new HashMap<>();

    static {
        categories.put("electro", Arrays.asList("electro-hub", "electro-swing"));
        categories.put("classical", Collections.singletonList("classical"));
        categories.put("korean", Collections.singletonList("korean-madness"));
        categories.put("hiphop", Collections.singletonList("hip-hop"));
        categories.put("chill", Collections.singletonList("chill-corner"));
        categories.put("metal", Collections.singletonList("metal-mix"));
        categories.put("retro", Collections.singletonList("retro-renegade"));
        categories.put("rock", Collections.singletonList("rock-n-roll"));
        categories.put("jazz", Collections.singletonList("coffee-house-jazz"));
        categories.put("pop", Collections.singletonList("purely-pop"));
    }

    public RequestSong(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "request.song";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onIntent(CommandMessage context, AIResponse response) {
        if (!isMusicEnabledInCategory(context)) {
            context.makeWarning("I can't play music for you because the music category is disabled for this channel :(").queue();
            return;
        }

        HashMap<String, JsonElement> parameters = response.getResult().getParameters();

        if (parameters.isEmpty() || !parameters.containsKey("music")) {
            context.makeWarning("Invalid or unsupported music category, more info coming here soon...").queue();
            return;
        }

        CommandContainer container = CommandHandler.getCommand(PlayCommand.class);
        String type = parameters.get("music").getAsString().toLowerCase();
        if (!categories.containsKey(type)) {
            container.getCommand().onCommand(
                new CommandMessage(container, context.getDatabaseEventHolder(), context.getMessage()), new String[]{type, "---leave-message"}
            );
            return;
        }

        String category = (String) RandomUtil.pickRandom(categories.get(type));
        String randomSong = getRandomSong(category);

        container.getCommand().onCommand(
            new CommandMessage(container, context.getDatabaseEventHolder(), context.getMessage()),
            new String[]{randomSong == null
                ? type : randomSong, "---leave-message"}
        );
    }

    private String getRandomSong(final String category) {
        Object cacheItem = avaire.getCache().getAdapter(CacheType.FILE).remember("music-type." + category.toLowerCase(), 31536000, () -> {
            try {
                String item = Jsoup.connect(
                    String.format("https://libraries.amped.fm/libraries/%s/musicbot", category)
                ).execute().body();

                String[] items = item.split("\n");

                return Arrays.asList(Arrays.copyOfRange(items, 2, items.length));
            } catch (IOException e) {
                AvaIre.getLogger().error("Failed to load {} music category from libraries.amped.fm", category.toLowerCase(), e);
                return null;
            }
        });

        if (!(cacheItem instanceof ArrayList)) {
            return null;
        }

        return (String) RandomUtil.pickRandom((List<String>) cacheItem);
    }

    private boolean isMusicEnabledInCategory(CommandMessage context) {
        if (context.getDatabaseEventHolder() == null || context.getDatabaseEventHolder().getGuild() == null) {
            return true;
        }

        ChannelTransformer channel = context.getDatabaseEventHolder()
            .getGuild().getChannel(context.getMessageChannel().getId());

        return channel == null || channel.isCategoryEnabled("music");
    }
}
