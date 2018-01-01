package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.CacheFingerprint;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@CacheFingerprint(name = "kick-command")
public class VoiceKickCommand extends Command {

    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    public VoiceKickCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Voice Kick Command";
    }

    @Override
    public String getDescription() {
        return "Kicks the mentioned user from the voice channel they're currently connected to, this action will be reported to any channel that has modloging enabled.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <user> [reason]` - Kicks the mentioned user with the given reason.");
    }

    @Override
    public String getExampleUsage() {
        return "`:command @Senither Yelling at people`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("voicekick", "vkick");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.kick_members",
            "require:bot,general.manage_channels,voice.move_members",
            "throttle:user,1,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (message.getMentionedUsers().isEmpty() || !userRegEX.matcher(args[0]).matches()) {
            return sendErrorMessage(message, "You must mention the user you want to kick.");
        }

        User user = message.getMentionedUsers().get(0);
        if (userHasHigherRole(user, message.getMember())) {
            return sendErrorMessage(message, "You can't kick people with a higher, or the same role as yourself.");
        }

        final Member member = message.getGuild().getMember(user);
        if (!member.getVoiceState().inVoiceChannel()) {
            return sendErrorMessage(message, "You can't voice kick people who are not connected to a voice channel.");
        }

        return kickUser(message, member, args);
    }

    private boolean kickUser(Message message, Member user, String[] args) {
        String reason = generateMessage(args);
        String originalVoiceChannelName = user.getVoiceState().getChannel().getName();
        message.getGuild().getController().createVoiceChannel("kick-" + user.getUser().getId()).queue(channel ->
            message.getGuild().getController().moveVoiceMember(user, (VoiceChannel) channel)
                .queue(empty -> channel.delete().queue(new Consumer<Void>() {
                        @Override
                        public void accept(Void empty) {
                            MessageFactory.makeSuccess(message, "**:target** was kicked from **:voiceChannel** by :user for \":reason\"")
                                .set("target", user.getUser().getName() + "#" + user.getUser().getDiscriminator())
                                .set("voiceChannel", originalVoiceChannelName)
                                .set("reason", reason)
                                .queue();
                        }
                    })
                )
        );
        return true;
    }

    private boolean userHasHigherRole(User user, Member author) {
        Role role = RoleUtil.getHighestFrom(author.getGuild().getMember(user));
        return role != null && RoleUtil.isRoleHierarchyHigher(author.getRoles(), role);
    }

    private String generateMessage(String[] args) {
        return args.length < 2 ?
            "No reason was given." :
            String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    }
}
