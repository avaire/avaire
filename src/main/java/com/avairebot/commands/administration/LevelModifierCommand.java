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
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.utility.RankCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.level.LevelManager;
import com.avairebot.utilities.ComparatorUtil;
import com.avairebot.utilities.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LevelModifierCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(LevelModifierCommand.class);

    public LevelModifierCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Level Modifier Command";
    }

    @Override
    public String getDescription() {
        return "The level modifier allows a server to set a custom level and experience modifier, allowing a server to fine tune the amount of XP required to level up by either making it harder or easier than default.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <percentage>` - Sets the level modifier to the given percentage.",
            "`:command reset` - Resets the level modifier back to the default value."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 500%` - Sets the modifier to 500%",
            "`:command 0.01%` - Sets the modifier to 0.01%",
            "`:command reset` - Resets the modifier back to default."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            AdministrateExperienceCommand.class,
            LevelHierarchyCommand.class,
            LevelAlertsCommand.class,
            ChannelLevelCommand.class,
            LevelCommand.class,
            RankCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("levelmodifier", "levelm");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "throttle:guild,1,5",
            "require:user,general.manage_server"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.LEVEL_AND_EXPERIENCE);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(context, "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            );
        }

        if (args.length == 0) {
            return sendStatusMessage(context, guildTransformer);
        }

        if (ComparatorUtil.isFuzzyFalse(args[0])) {
            return resetModifier(context, guildTransformer);
        }

        String argument = args[0];
        if (argument.endsWith("%")) {
            argument = argument.substring(0, argument.length() - 1);
        }

        try {
            double modifier = Double.parseDouble(argument);
            if (modifier < 0.001 || modifier > 500) {
                return sendErrorMessage(context, context.i18n("invalidNumberGiven"));
            }

            if (!updateModifierValue(context, guildTransformer, modifier / 100)) {
                return false;
            }

            context.makeSuccess(context.i18n("changedTo") + context.i18n("requiredIsNow"))
                .set("modifier", NumberUtil.formatNicelyWithDecimals(modifier) + "%")
                .addField(context.i18n("level", 5), getExperienceForLevel(guildTransformer, 5), true)
                .addField(context.i18n("level", 10), getExperienceForLevel(guildTransformer, 10), true)
                .addField(context.i18n("level", 25), getExperienceForLevel(guildTransformer, 25), true)
                .addField(context.i18n("level", 50), getExperienceForLevel(guildTransformer, 50), true)
                .addField(context.i18n("level", 100), getExperienceForLevel(guildTransformer, 100), true)
                .addField(context.i18n("level", 200), getExperienceForLevel(guildTransformer, 200), true)
                .queue();

            return true;
        } catch (NumberFormatException e) {
            return sendErrorMessage(context, context.i18n("invalidNumberGiven"));
        }
    }

    private boolean resetModifier(CommandMessage context, GuildTransformer guildTransformer) {
        if (!updateModifierValue(context, guildTransformer, -1)) {
            return false;
        }

        context.makeSuccess(context.i18n("resetToDefault") + context.i18n("requiredIsNow"))
            .set("modifier", NumberUtil.formatNicelyWithDecimals(LevelManager.getDefaultModifier() * 100) + "%")
            .addField(context.i18n("level", 5), getExperienceForLevel(guildTransformer, 5), true)
            .addField(context.i18n("level", 10), getExperienceForLevel(guildTransformer, 10), true)
            .addField(context.i18n("level", 25), getExperienceForLevel(guildTransformer, 25), true)
            .addField(context.i18n("level", 50), getExperienceForLevel(guildTransformer, 50), true)
            .addField(context.i18n("level", 100), getExperienceForLevel(guildTransformer, 100), true)
            .addField(context.i18n("level", 200), getExperienceForLevel(guildTransformer, 200), true)
            .queue();

        return true;
    }

    private boolean sendStatusMessage(CommandMessage context, GuildTransformer guildTransformer) {
        context.makeSuccess(context.i18n("statusMessage"))
            .set("modifier", NumberUtil.formatNicelyWithDecimals((guildTransformer.getLevelModifier() < 0
                ? LevelManager.getDefaultModifier() * 100
                : guildTransformer.getLevelModifier() * 100)
            ) + "%")
            .addField(context.i18n("level", 5), getExperienceForLevel(guildTransformer, 5), true)
            .addField(context.i18n("level", 10), getExperienceForLevel(guildTransformer, 10), true)
            .addField(context.i18n("level", 25), getExperienceForLevel(guildTransformer, 25), true)
            .addField(context.i18n("level", 50), getExperienceForLevel(guildTransformer, 50), true)
            .addField(context.i18n("level", 100), getExperienceForLevel(guildTransformer, 100), true)
            .addField(context.i18n("level", 200), getExperienceForLevel(guildTransformer, 200), true)
            .queue();

        return true;
    }

    private String getExperienceForLevel(GuildTransformer guildTransformer, int level) {
        return NumberUtil.formatNicely(
            avaire.getLevelManager().getExperienceFromLevel(guildTransformer, level)
                - avaire.getLevelManager().getExperienceFromLevel(guildTransformer, 0)
        ) + " XP";
    }

    private boolean updateModifierValue(CommandMessage context, GuildTransformer transformer, double value) {
        try {
            avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("level_modifier", value < 0 ? null : value));

            transformer.setLevelModifier(value);

            return true;
        } catch (SQLException e) {
            log.error("A SQLException was thrown while trying to update the level modifier for {}, error: {}",
                context.getGuild().getId(), e.getMessage(), e
            );

            return sendErrorMessage(context, "Failed to update the servers level modifier, error: ", e.getMessage());
        }
    }
}
