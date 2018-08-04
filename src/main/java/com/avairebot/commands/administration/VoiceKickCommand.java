package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.CacheFingerprint;
import com.avairebot.contracts.commands.Command;
import com.avairebot.modlog.ModlogAction;
import com.avairebot.modlog.ModlogModule;
import com.avairebot.modlog.ModlogType;
import com.avairebot.utilities.MentionableUtil;
import com.avairebot.utilities.RestActionUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@CacheFingerprint(name = "kick-command")
public class VoiceKickCommand extends Command {

    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);

    public VoiceKickCommand(AvaIre avaire) {
        super(avaire, false);
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
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command @Senither Yelling at people`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(KickCommand.class);
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
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = MentionableUtil.getUser(context, args);
        if (user == null) {
            return sendErrorMessage(context, "You must mention the user you want to kick.");
        }

        if (userHasHigherRole(user, context.getMember())) {
            return sendErrorMessage(context, "You can't kick people with a higher, or the same role as yourself.");
        }

        final Member member = context.getGuild().getMember(user);
        if (!member.getVoiceState().inVoiceChannel()) {
            return sendErrorMessage(context, "You can't voice kick people who are not connected to a voice channel.");
        }

        return kickUser(context, member, args);
    }

    private boolean kickUser(CommandMessage context, Member user, String[] args) {
        String reason = generateMessage(args);
        String originalVoiceChannelName = user.getVoiceState().getChannel().getName();
        String originalVoiceChannelId = user.getVoiceState().getChannel().getId();

        context.getGuild().getController().createVoiceChannel("kick-" + user.getUser().getId()).queue(channel ->
            context.getGuild().getController().moveVoiceMember(user, (VoiceChannel) channel)
                .queue(empty -> channel.delete().queue(new Consumer<Void>() {
                        @Override
                        public void accept(Void empty) {
                            ModlogModule.log(avaire, context, new ModlogAction(
                                    ModlogType.VOICE_KICK,
                                    context.getAuthor(), user.getUser(),
                                    originalVoiceChannelName + " (ID: " + originalVoiceChannelId + ")\n" + reason
                                )
                            );

                            context.makeSuccess("**:target** was kicked from **:voiceChannel** by :user for \":reason\"")
                                .set("target", user.getUser().getName() + "#" + user.getUser().getDiscriminator())
                                .set("voiceChannel", originalVoiceChannelName)
                                .set("reason", reason)
                                .queue();
                        }
                    }, RestActionUtil.ignore)
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
