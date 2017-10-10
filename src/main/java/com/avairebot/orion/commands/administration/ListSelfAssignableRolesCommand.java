package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListSelfAssignableRolesCommand extends Command {

    public ListSelfAssignableRolesCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "List Self Assignable Roles Command";
    }

    @Override
    public String getDescription() {
        return "List all the self-assignable roles, 10 per-page.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("lsar");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        GuildTransformer transformer = GuildController.fetchGuild(orion, message);

        if (transformer.getSelfAssignableRoles().isEmpty()) {
            MessageFactory.makeWarning(message, "There are currently no self-assignable roles for this server.").queue();
            return true;
        }

        int pageNumber = 1;
        if (args.length > 0) {
            try {
                pageNumber = Integer.parseInt(args[0], 10);
                if (pageNumber < 1) {
                    pageNumber = 1;
                }
            } catch (NumberFormatException ex) {
                pageNumber = 1;
            }
        }

        ArrayList<String> items = new ArrayList<>(transformer.getSelfAssignableRoles().values());
        Collections.sort(items);

        int pages = (int) Math.ceil(items.size() / 10);
        pageNumber = Math.max(1, Math.min(pageNumber, pages));

        List<String> messages = new ArrayList<>();
        int start = 10 * (pageNumber - 1);
        for (int i = start; i < start + 10; i++) {
            if (items.size() <= i) {
                break;
            }

            messages.add(String.format("**%s**", items.get(i)));
        }

        messages.add(String.format("\nPage **%s** out of **%s** pages.", pageNumber, pages == 0 ? 1 : pageNumber));
        messages.add(String.format("`%s [page number]`", generateCommandTrigger(message)));

        message.getChannel().sendMessage(MessageFactory.createEmbeddedBuilder()
                .setColor(MessageFactory.MessageType.SUCCESS.getColor())
                .setTitle(String.format("There are %s self-assignable roles", items.size()))
                .setDescription(String.join("\n", messages)).build()).queue();

        return true;
    }
}
