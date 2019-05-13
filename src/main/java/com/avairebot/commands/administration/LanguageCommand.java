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

package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.language.Language;
import com.avairebot.utilities.NumberUtil;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LanguageCommand extends Command {

    public LanguageCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Language Command";
    }

    @Override
    public String getDescription() {
        return "Show a list of available languages or set a language that should be used for the server.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command [page]` - Displays a list of languages, 10 languages per page.",
            "`:command [code]` - Sets the language to the given language code."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 2` - Displays the languages on page 2",
            "`:command english` - Changes the language of the bot to English"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("language", "lang");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.COMMAND_CUSTOMIZATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendLanguageList(context, 1);
        }

        if (NumberUtil.isNumeric(args[0])) {
            return sendLanguageList(context, NumberUtil.parseInt(args[0], 1));
        }

        Language language = Language.parse(String.join(" ", args));
        if (language == null) {
            return sendErrorMessage(
                context, context.i18n("invalidLanguageCode", String.join(" ", args))
            );
        }

        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("local", language.getCode()));
            transformer.setLocale(language.getCode());

            context.makeSuccess(context.i18n("changed"))
                .set("name", language.getNativeName())
                .queue();
        } catch (SQLException e) {
            AvaIre.getLogger().error("Failed to update the language for a server({}), error: " + e.getMessage(),
                context.getGuild().getId()
            );
            return sendErrorMessage(context, "Failed to update the servers language settings, please try again, if this problem persists, please contact one of the bot developers about it.");
        }

        return true;
    }

    private boolean sendLanguageList(CommandMessage context, int pageNum) {
        List<String> items = new ArrayList<>();
        for (Language lang : Language.values()) {
            items.add(String.format("`%s` %s", lang.getCode(), lang.getNativeName()));
        }

        SimplePaginator<String> paginator = new SimplePaginator<>(items, 10, pageNum);

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> messages.add(val));

        context.makeInfo(":note\n\n:languages\n\n:paginator")
            .set("note", context.i18n("note", generateCommandTrigger(context.message)))
            .set("languages", String.join("\n", messages))
            .set("paginator", paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())))
            .queue();

        return false;
    }
}
