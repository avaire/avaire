package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UndertaleTextBoxCommand extends Command {

    private String templateUrl = "www.demirramon.com/utgen.png?message=%s&character=%s";

    public UndertaleTextBoxCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Undertale TextBox Command";
    }

    @Override
    public String getDescription() {
        return "TODO";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command [message] [character]` - Returns an undertale textbox image containing the specified text."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command toriel Greetings,my child`");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("utbox", "textbox");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("missingArgument"));
        }

        try {
            context.makeEmbeddedMessage()
                .setImage(String.format(
                    templateUrl,
                    URLEncoder.encode(args[0], "UTF-8").replace("+", "%20"),
                    args[1].toLowerCase()
                )).queue();
        } catch (UnsupportedEncodingException e) {
            context.makeError(e.getMessage());
            return false;
        }

        return true;
    }
}
