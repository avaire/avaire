package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.language.I18n;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UndertaleTextBoxCommand extends Command {

    private final String templateUrl = "https://www.demirramon.com/gen/undertale_box.png?&ext=.png";
    private final String messageQueryString = "&message={0}";
    private final String characterQueryString = "&character={1}";
    private final String urlQueryString = "&url={1}";

    public UndertaleTextBoxCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Undertale TextBox Command";
    }

    @Override
    public String getDescription() {
        return "Create your own Undertale text boxes with any character and text! \n" +
            " Generator owned by  Demirramon. Undertale owned by Toby Fox. All rights reserved. ";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <url> <message>` - Returns an undertale textbox image containing" +
                " the specified text said by the custom character provided in the picture URL.\n"
                + "If you need to upload the picture you want to use, use Imgur.",
            "`:command <message>` - Returns an undertale textbox image containing the specified text.",
            "`:command <character> <message>` - Returns an undertale textbox image containing the specified text " +
                "said by the character of your choice."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList("`:command Toriel Greetings, my child `", "`:command https://www.demirramon.com/img/generators/utgen/char_undertale-frisk_default.png I'm Frisk `");
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
        if (args.length == 0 || (args.length == 1 && NumberUtil.isNumeric(args[0]))) {
            return sendCharacterList(context, args);
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

            if (!isValidImageUrl(args[0])) {
                InputStream stream = new URL(I18n.format(
                    templateUrl + messageQueryString + characterQueryString,
                    encode(String.join(" ", Arrays.copyOfRange(args, 1, args.length))),
                    encode(args[0])
                )).openStream();

                context.getChannel().sendFile(stream, getClass().getSimpleName() + "-" + args[0] + ".png", messageBuilder.build()).queue();

                return true;
            } else {


                InputStream stream = new URL(I18n.format(
                    templateUrl + messageQueryString + urlQueryString,
                    encode(String.join(" ", Arrays.copyOfRange(args, 1, args.length))),
                    args[0]
                ) + "&character=custom").openStream();

                context.getChannel().sendFile(stream, getClass().getSimpleName() + "-" + args[0] + ".png", messageBuilder.build()).queue();

                return true;
            }
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

    private boolean isValidImageUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String stringUri = url.toURI().toString();
            List<String> fileFormats = new ArrayList<>();
            fileFormats.add("png");
            fileFormats.add("jpg");
            if (stringUri.contains(".")) {
                for (String fileFormat : fileFormats) {
                    if (fileFormat.equalsIgnoreCase(stringUri.substring(stringUri.lastIndexOf('.') + 1))) {
                        return true;
                    }
                }

                return false;
            } else {
                return false;
            }

        } catch (Exception exception) {
            return false;
        }
    }

    private boolean sendCharacterList(CommandMessage context, String[] args) {
        List<String> items = new ArrayList<>();
        for (Character character : Character.values()) {
            items.add(character.name());
        }

        SimplePaginator paginator = new SimplePaginator(items, 10);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> messages.add((String) val));

        context.makeInfo(":characters\n\n:paginator")
            .setTitle("Undertale Character List")
            .set("characters", String.join("\n", messages))
            .set("paginator", paginator.generateFooter(generateCommandTrigger(context.getMessage())))
            .queue();
        return false;
    }

    private enum Character {
        Alphys, Asgore, Asriel, Chara, Flowey, Frisk, Gaster, Grillby, Mettaton,
        MettatonEX, Napstablook, OmegaFlowey, Papyrus, Sans, Temmie, Undyne
    }
}
