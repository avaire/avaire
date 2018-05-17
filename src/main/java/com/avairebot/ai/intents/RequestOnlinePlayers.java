package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.ai.Intent;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;

public class RequestOnlinePlayers extends Intent {

    public RequestOnlinePlayers(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "request.online-players";
    }

    @Override
    public void onIntent(CommandMessage context, AIResponse response) {
        if (!context.getMessage().getChannelType().isGuild()) {
            context.makeWarning("Right now it's just me and you online ;)").queue();
            return;
        }

        int online = 0;
        for (Member member : context.getGuild().getMembers()) {
            if (!member.getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
                online++;
            }
        }

        context.makeInfo("There are **:online** people online out of **:total** people on the server.")
            .set("online", online)
            .set("total", context.getGuild().getMembers().size())
            .queue();
    }
}
