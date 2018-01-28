package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.permissions.Permissions;
import com.avairebot.utilities.RoleUtil;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;

public class GuildMemberJoin extends EventHandler {

    /**
     * Instantiates the event handler and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public GuildMemberJoin(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, event.getGuild());

        for (ChannelTransformer channelTransformer : transformer.getChannels()) {
            if (channelTransformer.getWelcome().isEnabled()) {
                TextChannel textChannel = event.getGuild().getTextChannelById(channelTransformer.getId());
                if (textChannel == null) {
                    continue;
                }

                if (!event.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)) {
                    continue;
                }

                textChannel.sendMessage(StringReplacementUtil.parseGuildJoinLeaveMessage(
                    event.getGuild(), textChannel, event.getUser(),
                    channelTransformer.getWelcome().getMessage() == null ?
                        "Welcome %user% to **%server%!**" :
                        channelTransformer.getWelcome().getMessage())
                ).queue();
            }
        }

        if (event.getUser().isBot()) {
            return;
        }

        if (transformer.getAutorole() != null) {
            Role role = event.getGuild().getRoleById(transformer.getAutorole());
            if (canGiveRole(event, role)) {
                event.getGuild().getController().addSingleRoleToMember(
                    event.getMember(), role
                ).queue();
            }
        }
    }

    private boolean canGiveRole(GuildMemberJoinEvent event, Role role) {
        return role != null
            && event.getGuild().getSelfMember().hasPermission(Permissions.MANAGE_ROLES.getPermission())
            && RoleUtil.isRoleHierarchyLower(event.getGuild().getSelfMember().getRoles(), role);
    }
}
