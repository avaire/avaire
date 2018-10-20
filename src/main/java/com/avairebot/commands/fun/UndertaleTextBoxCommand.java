package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.language.I18n;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UndertaleTextBoxCommand extends Command {

    private final String templateUrl = "https://www.demirramon.com/gen/undertale_box.png?character={0}&message={1}&ext=.png";

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
            "`:command <character> <message>` - Returns an undertale textbox image containing the specified text."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command toriel Greetings, my child`");
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
            return sendErrorMessage(context, "errors.missingArgument", "character");
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "message");
        }

        try {
            MessageBuilder messageBuilder = new MessageBuilder();
            EmbedBuilder embedBuilder = context.makeEmbeddedMessage()
                .setImage("attachment://" + getClass().getSimpleName() + "-" + args[0] + ".png")
                .requestedBy(context)
                .build();

            messageBuilder.setEmbed(embedBuilder.build());

            InputStream stream = new URL(I18n.format(
                templateUrl,
                encode(args[0]),
                encode(String.join(" ", Arrays.copyOfRange(args, 1, args.length)))
            )).openStream();

            context.getChannel().sendFile(stream, getClass().getSimpleName() + "-" + args[0] + ".png", messageBuilder.build()).queue();

            return true;
        } catch (UnsupportedEncodingException e) {
            context.makeError(e.getMessage());
        } catch (IOException e) {
            context.makeError(e.getMessage());
        }

        return false;
    }

    private String encode(String string) throws UnsupportedEncodingException {
        return URLEncoder.encode(string, "UTF-8").replace("+", "%20");
    }
}
