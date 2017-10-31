package com.avairebot.orion.handlers;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.handlers.EventHandler;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.ChannelTransformer;
import com.avairebot.orion.database.transformers.GuildTransformer;
import com.avairebot.orion.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

public class GuildMemberLeave extends EventHandler {

    /**
     * Instantiates the event handler and sets the orion class instance.
     *
     * @param orion The Orion application class instance.
     */
    public GuildMemberLeave(Orion orion) {
        super(orion);
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(orion, event.getGuild());

        for (ChannelTransformer channelTransformer : transformer.getChannels()) {
            if (channelTransformer.getGoodbye().isEnabled()) {
                TextChannel textChannel = event.getGuild().getTextChannelById(channelTransformer.getId());
                if (textChannel == null) {
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
