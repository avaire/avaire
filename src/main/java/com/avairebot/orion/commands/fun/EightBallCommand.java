package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.RandomUtil;
import net.dv8tion.jda.core.entities.Message;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EightBallCommand extends Command {

    private final List<String> answers = Arrays.asList(
        "It is certain",
        "It is decidedly so",
        "Without a doubt",
        "Yes definitely",
        "You may rely on it",
        "As I see it, yes",
        "Most likely",
        "Outlook good",
        "Yes",
        "Signs point to yes",
        "Reply hazy try again",
        "Ask again later",
        "Better not tell you now",
        "Cannot predict now",
        "Concentrate and ask again",
        "Don't count on it",
        "My reply is no",
        "My sources say no",
        "Outlook not so good",
        "Very doubtful"
    );

    public EightBallCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Eight Ball Command";
    }

    @Override
    public String getDescription() {
        return "Ask 8Ball a question and get a random response back.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <question>`");
    }

    @Override
    public String getExampleUsage() {
        return "`:command will i have a good day today?`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("8ball");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "You must include a question for 8ball.");
        }

        message.getChannel().sendMessage(MessageFactory.createEmbeddedBuilder()
            .setColor(Color.decode("#2A2C31"))
            .setDescription((CharSequence) RandomUtil.pickRandom(answers))
            .build()
        ).queue();

        return true;
    }
}
