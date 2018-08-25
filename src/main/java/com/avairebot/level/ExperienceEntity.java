package com.avairebot.level;

public class ExperienceEntity {

    private final long userId;
    private final long guildId;
    private int experience;

    ExperienceEntity(long userId, long guildId, int experience) {
        this.userId = userId;
        this.guildId = guildId;
        this.experience = experience;
    }

    /**
     * The ID of the user that should be updated.
     *
     * @return The ID of the user that should be updated.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * The ID of the guild that the user was on when they got the XP reward.
     *
     * @return The ID of the guild that the user was on when they got an XP reward.
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * The amount of experience the user has in total for the set guild.
     *
     * @return The total amount of experience the user has for the set guild.
     */
    public int getExperience() {
        return experience;
    }

    /**
     * Sets the amount of experience that the user should be have after
     * the experience entity is consumed and synced with the database.
     *
     * @param experience The amount of experience the user should have after
     *                   the entity is synced with the database.
     */
    public void setExperience(int experience) {
        this.experience = experience;
    }

    @Override
    public String toString() {
        return String.format("[userId:%s, guildId:%s, experience:%s]",
            userId, guildId, experience
        );
    }
}
