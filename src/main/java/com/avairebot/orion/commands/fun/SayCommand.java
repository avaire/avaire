package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SayCommand extends AbstractCommand {

    public SayCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Say Comand";
    }

    @Override
    public String getDescription() {
        return "I will say whatever you tell me to.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <message>` - Makes the bot say the given message");
    }

    @Override
    public String getExampleUsage() {
        return "`:command I am a BOT`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("say");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("require:all,text.manage_messages");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing `message` argument, the `message` argument is required!");
        }

        String[] split = message.getContent().split(" ");
        message.getChannel().sendMessage(String.join(" ", Arrays.copyOfRange(split, 1, split.length))).queue();
        message.delete().reason("Orion Say command usage").queue();

        return true;
    }
}
