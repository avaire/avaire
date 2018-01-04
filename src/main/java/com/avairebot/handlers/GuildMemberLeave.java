package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

public class GuildMemberLeave extends EventHandler {

    /**
     * Instantiates the event handler and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public GuildMemberLeave(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(avaire, event.getGuild());

        for (ChannelTransformer channelTransformer : transformer.getChannels()) {
            if (channelTransformer.getGoodbye().isEnabled()) {
                TextChannel textChannel = event.getGuild().getTextChannelById(channelTransformer.getId());
                if (textChannel == null) {
                    continue;
                }

                if (!event.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)) {
                    continue;
                }

                textChannel.sendMessage(
                    StringReplacementUtil.parseChannel(textChannel,
                        StringReplacementUtil.parseUser(event.getUser(),
                            StringReplacementUtil.parseGuild(event.getGuild(),
                                channelTransformer.getGoodbye().getMessage() == null ?
                                    "%user% has left **%server%**! :(" :
                                    channelTransformer.getGoodbye().getMessage()
                            )
                        )
                    )
                ).queue();
            }
        }
    }
}
