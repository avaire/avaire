package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.chat.MessageType;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogModule;
import com.avairebot.modlog.ModlogType;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.User;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WarnCommand extends Command {

    public WarnCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Warn Command";
    }

    @Override
    public String getDescription() {
        return "Warns a given user with a message, this action will be reported to any channel that has modloging enabled.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <user> [reason]` - Warns the mentioned user with the given reason."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command @Senither Being a potato` - Warns Senither for being a potato."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("warn");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,text.manage_messages",
            "throttle:channel,1,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (transformer.getModlog() == null) {
            String prefix = generateCommandPrefix(context.getMessage());
            return sendErrorMessage(context,
                "This command requires a modlog channel to be set, a modlog channel can be set using the `{0}modlog` command.", prefix
            );
        }

        User user = null;
        if (args.length > 0) {
            user = MentionableUtil.getUser(context, args);
        }

        if (user == null) {
            return sendErrorMessage(context, "You must mention a user you want warn.");
        }

        if (user.isBot()) {
            return sendErrorMessage(context, "You can't warn bots!");
        }

        String reason = "No reason was given.";
        if (args.length > 1) {
            reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }

        String caseId = ModlogModule.log(avaire, context.getGuild(), transformer, new ModlogAction(
                ModlogType.WARN,
                context.getAuthor(), user,
                reason
            )
        );

        if (caseId == null) {
            return sendErrorMessage(context, "Failed to log warning to the set modlog channel, does the modlog channel still exists, can I still send messages in the channel?");
        }

        User finalUser = user;
        String finalReason = reason;
        user.openPrivateChannel().queue(message -> {
            message.sendMessage(
                MessageFactory.createEmbeddedBuilder()
                    .setColor(MessageType.WARNING.getColor())
                    .setDescription("You have been **warned** in " + context.getGuild().getName())
                    .addField("Moderator", context.getAuthor().getName() + "#" + context.getAuthor().getDiscriminator(), true)
                    .addField("Reason", finalReason, true)
                    .setFooter("Case ID #" + caseId, null)
                    .setTimestamp(Instant.now())
                    .build()
            ).queue(null, RestActionUtil.ignore);

            context.makeWarning(":target has been **warned** for \":reason\"")
                .set("target", finalUser.getName() + "#" + finalUser.getDiscriminator())
                .set("reason", finalReason)
                .setFooter("Case ID #" + caseId)
                .setTimestamp(Instant.now())
                .queue(null, RestActionUtil.ignore);
        }, error -> {
            context.makeWarning("Failed to DM the user with the warning, they most likely have their private settings set to disable all DMs from this server.")
                .queue();
        });

        return true;
    }
}
