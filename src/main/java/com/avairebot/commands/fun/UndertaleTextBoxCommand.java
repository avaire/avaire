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
import java.util.regex.Pattern;

@SuppressWarnings("FieldCanBeLocal")
public class UndertaleTextBoxCommand extends Command {

    private final Pattern imageRegex = Pattern.compile("(http(s?):)([/|.|\\w|\\s|-])*\\.(?:jpg|jpeg|gif|png)");

    private final String templateUrl = "https://www.demirramon.com/gen/undertale_box.png?message={0}";
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
        return "Create your own Undertale text boxes with any character and text you want, you can also specify a image through a URL that should be used as the avatar instead.!\n" +
            "Generator owned by [Demirramon](https://demirramon.com/). Undertale owned by Toby Fox. All rights reserved.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command list [page]` - Lists some undertale characters the generator supports.",
            "`:command <url> <message>` - Generates the image using the given image url and message.",
            "`:command <character> <message>` - Generates the image using the provided undertale character as the avatar, and the provided message."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command Toriel Greetings, my child`",
            "`:command https://i.imgur.com/ZupgGkI.jpg Want to play?`"
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("undertale", "ut");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("noArgumentsGiven"));
        }

        if (args.length < 3 && args[0].equalsIgnoreCase("list")) {
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

            InputStream stream = getImageInputStream(args);
            context.getMessageChannel().sendFile(stream, getClass().getSimpleName() + "-" + args[0] + ".png", messageBuilder.build()).queue();

            return true;
        } catch (IOException e) {
            return sendErrorMessage(context, context.i18n("failedToGenerateImage"));
        }
    }

    private InputStream getImageInputStream(String[] args) throws IOException {
        boolean isValidImageUrl = imageRegex.matcher(args[0]).find();

        return new URL(I18n.format(
            templateUrl + (isValidImageUrl ? urlQueryString : characterQueryString),
            encode(String.join(" ", Arrays.copyOfRange(args, 1, args.length))),
            encode(args[0])
        ) + (isValidImageUrl ? "&character=custom" : "")).openStream();
    }

    private String encode(String string) throws UnsupportedEncodingException {
        return URLEncoder.encode(string, "UTF-8").replace("+", "%20");
    }

    private boolean sendCharacterList(CommandMessage context, String[] args) {
        SimplePaginator<String> paginator = new SimplePaginator<>(Character.names, 10);
        if (args.length > 1) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
        }

        List<String> messages = new ArrayList<>();
        paginator.forEach((index, key, val) -> messages.add(val));

        context.makeInfo(":characters\n\n:paginator")
            .setTitle(context.i18n("title"))
            .set("characters", String.join("\n", messages))
            .set("paginator", paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())))
            .queue();

        return false;
    }

    private enum Character {
        Alphys, Asgore, Asriel, Chara, Flowey, Frisk, Gaster, Grillby, Mettaton,
        MettatonEX, Napstablook, OmegaFlowey, Papyrus, Sans, Temmie, Undyne;

        private static final List<String> names;

        static {
            List<String> characterNames = new ArrayList<>();
            for (Character character : values()) {
                characterNames.add(character.name());
            }
            names = Collections.unmodifiableList(characterNames);
        }
    }
}
