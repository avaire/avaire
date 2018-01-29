package com.avairebot.handlers;

import com.avairebot.AvaIre;
import com.avairebot.contracts.handlers.EventHandler;
import com.avairebot.database.controllers.PlayerController;
import net.dv8tion.jda.core.events.user.UserAvatarUpdateEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;

public class UserUpdateEvent extends EventHandler {

    public UserUpdateEvent(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public void onUserAvatarUpdate(UserAvatarUpdateEvent event) {
        PlayerController.updateUserData(avaire, event.getUser());
    }

    @Override
    public void onUserNameUpdate(UserNameUpdateEvent event) {
        PlayerController.updateUserData(avaire, event.getUser());
    }
}
