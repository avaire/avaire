package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.shared.DiscordConstants;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ModlogReasonCommand extends Command {

    public ModlogReasonCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Modlog Reason Command";
    }

    @Override
    public String getDescription(CommandContext context) {
        String prefix = context.isGuildMessage() ? generateCommandPrefix(context.getMessage()) : DiscordConstants.DEFAULT_COMMAND_PREFIX;

        return String.format(
            "Sets the reason for an old modlog case, this command requires the server has a modlog channel set using the `%smodlog` command.\n%s",
            prefix, "You can only set modlog reasons for old modlog cases if you were the moderator for the case."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <case id> <reason>` - Sets the reason for the given ID"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command 9 Advertising stuff in #general` - Sets the 9th modlog case to \"Advertising stuff in #general\""
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            ModlogCommand.class,
            ModlogHistoryCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("reason");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList(
            "throttle:user,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (transformer.getModlog() == null) {
            return sendErrorMessage(context, "No modlog channel has been set, you must set a modlog channel to use this command.");
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "case id");
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "reason");
        }

        int caseId = NumberUtil.parseInt(args[0], -1);
        if (caseId < 1 || caseId > transformer.getModlogCase()) {
            return sendErrorMessage(context, "Invalid case id given, the ID must be greater than 0 and less than {0}", "" + transformer.getModlogCase());
        }

        final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        try {
            Collection collection = avaire.getDatabase().newQueryBuilder(Constants.LOG_TABLE_NAME)
                .where("guild_id", context.getGuild().getId())
                .where("user_id", context.getAuthor().getId())
                .where("modlogCase", caseId)
                .get();

            if (collection.isEmpty()) {
                return sendErrorMessage(context, "Couldn't find a modlog case with an ID of {0} where you were the moderator, are you sure you're the moderator for the given modlog case?", "" + caseId);
            }

            DataRow first = collection.first();

            TextChannel channel = context.getGuild().getTextChannelById(transformer.getModlog());
            if (channel == null) {
                return sendErrorMessage(context, "Couldn't find the modlog channel, was it removed?");
            }

            channel.getMessageById(first.getString("message_id")).queue(message -> {
                if (message.getEmbeds().isEmpty()) {
                    // What? This code should never be hit...
                    return;
                }

                EmbedBuilder embeddedBuilder = MessageFactory.createEmbeddedBuilder();

                MessageEmbed embed = message.getEmbeds().get(0);

                embeddedBuilder.setTitle(embed.getTitle());
                embeddedBuilder.setDescription(embed.getDescription());
                embeddedBuilder.setColor(embed.getColor());
                embeddedBuilder.setTimestamp(embed.getTimestamp());

                if (embed.getFooter() != null) {
                    embeddedBuilder.setFooter(embed.getFooter().getText(), null);
                }

                for (MessageEmbed.Field field : embed.getFields()) {
                    if (!field.getName().equalsIgnoreCase("Reason")) {
                        embeddedBuilder.addField(field);
                        continue;
                    }

                    embeddedBuilder.addField("Reason", reason, field.isInline());
                }

                message.editMessage(embeddedBuilder.build()).queue(newMessage -> {
                    context.makeSuccess("The modlog case with an ID of **:id** was successfully edited and set to the reason of `:reason`")
                        .set("id", caseId)
                        .set("reason", reason)
                        .queue(successMessage -> successMessage.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));
                    context.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore);
                }, error -> {
                    context.makeError("Failed to edit modlog message: " + error.getMessage())
                        .queue(successMessage -> successMessage.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));
                    context.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore);
                });
            }, error -> {
                context.makeWarning("Couldn't find the message for the given modlog case, was it deleted?")
                    .queue(null, RestActionUtil.ignore);
            });
        } catch (SQLException error) {
            return sendErrorMessage(context, "Something went wrong while trying to edit the modlog message: " + error.getMessage());
        }

        return true;
    }
}
