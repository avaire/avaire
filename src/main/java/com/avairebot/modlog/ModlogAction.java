package com.avairebot.modlog;

import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;

public class ModlogAction {

    private final User moderator;
    private final User target;

    private ModlogType type;
    private String message = null;

    public ModlogAction(@Nonnull ModlogType action, @Nonnull User moderator, User target) {
        this(action, moderator, target, null);
    }

    public ModlogAction(@Nonnull ModlogType action, @Nonnull User moderator, User target, String message) {
        this.moderator = moderator;
        this.target = target;
        this.type = action;
        this.message = message;
    }

    public User getModerator() {
        return moderator;
    }

    public String getStringifiedModerator() {
        return stringifyUser(moderator);
    }

    public User getTarget() {
        return target;
    }

    public String getStringifiedTarget() {
        return stringifyUser(target);
    }

    public ModlogType getType() {
        return type;
    }

    public void setType(ModlogType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String stringifyUser(User user) {
        if (user == null) {
            return "";
        }
        return user.getName() + "#" + user.getDiscriminator() + " (" + user.getAsMention() + ")";
    }
}
