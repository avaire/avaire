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

package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheType;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.User;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class MemeCommand extends Command {

    private final String customUrl = "https://memegen.link/custom/%s/%s.jpg?size=256&alt=%s&discordFormat=some-avatar.png";
    private final String templateUrl = "https://memegen.link/%s/%s/%s.jpg";

    private final Map<String, Map<String, String>> memes = new HashMap<>();
    private final List<String> memeKeys = new ArrayList<>();

    public MemeCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Meme Command";
    }

    @Override
    public String getDescription() {
        return "Generates memes with your given text, you can tag users to use their avatar as a meme, or just give the meme name you wanna use.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command list` - Lists all the available meme types.",
            "`:command <meme> <top text> <bottom text>` - Generates the meme with the given text.",
            "`:command <user> <top text> <bottom text>` - Generates a meme with the tagged users avatar and the given text."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command buzz \"Memes\" \"Memes everywhere\"`",
            "`:command @Senither \"Creates a Meme command for AvaIre\" \"Almost no one uses it\"`"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("meme");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,6");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("missingArgument"));
        }

        if (memes.isEmpty() || memeKeys.isEmpty()) {
            loadMemesIntoMemory();
        }

        if (args[0].equalsIgnoreCase("list")) {
            return sendMemeList(context, Arrays.copyOfRange(args, 1, args.length));
        }

        if (!context.getMentionedUsers().isEmpty()) {
            return sendUserMeme(context, context.getMentionedUsers().get(0), Arrays.copyOfRange(args, 1, args.length));
        }

        if (memeKeys.contains(args[0].toLowerCase())) {
            return sendGeneratedMeme(context, args[0].toLowerCase(), Arrays.copyOfRange(args, 1, args.length));
        }

        return sendErrorMessage(context, context.i18n("invalidType", args[0]));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private boolean sendMemeList(CommandMessage context, String[] args) {
        if (memes.isEmpty() || memeKeys.isEmpty()) {
            loadMemesIntoMemory();
        }

        SimplePaginator<String> paginator = new SimplePaginator<>(memeKeys, 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        final List<String> memesMessages = new ArrayList<>();
        paginator.forEach((index, key, val) -> memesMessages.add(
            context.i18n("listItem", val, memes.get(val).get("name"))
        ));

        context.makeSuccess(String.format("%s\n\n%s",
            String.join("\n", memesMessages),
            paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage()) + " list")
        )).setTitle("Memes").queue();

        // We're returning false here to prevent the Meme command from
        // being throttled for users just wanting to see what types
        // of memes are available without generating any memes.
        return false;
    }

    private boolean sendUserMeme(CommandMessage context, User user, String[] args) {
        if (args.length < 2) {
            return sendErrorMessage(context, context.i18n("mustIncludeTopAndBottomText"));
        }

        try {
            context.makeEmbeddedMessage()
                .setImage(String.format(
                    customUrl,
                    formatMemeArgument(args[0]),
                    formatMemeArgument(args[1]),
                    URLEncoder.encode(user.getEffectiveAvatarUrl(), "UTF-8")
                )).queue();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean sendGeneratedMeme(CommandMessage context, String meme, String[] args) {
        if (args.length < 2) {
            return sendErrorMessage(context, context.i18n("mustIncludeTopAndBottomText"));
        }

        context.makeEmbeddedMessage()
            .setImage(String.format(
                templateUrl,
                meme,
                formatMemeArgument(args[0]),
                formatMemeArgument(args[1])
            )).queue();

        return true;
    }

    private String formatMemeArgument(String string) {
        return string.trim()
            .replaceAll("_", "__")
            .replaceAll("-", "--")
            .replaceAll(" ", "_")
            .replaceAll("\\?", "~q")
            .replaceAll("%", "~p")
            .replaceAll("#", "~h")
            .replaceAll("/", "~s")
            .replaceAll("''", "\"");
    }

    private void loadMemesIntoMemory() {
        Map<String, Map<String, String>> cachedMemes = (Map<String, Map<String, String>>) avaire.getCache().getAdapter(CacheType.FILE).get("meme.types");
        List<String> keys = new ArrayList<>(cachedMemes.keySet());
        Collections.sort(keys);

        memeKeys.clear();
        memeKeys.addAll(keys);

        memes.clear();
        memes.putAll(cachedMemes);
    }
}
