package com.avairebot.contracts.shard;

import com.avairebot.shard.AvaireShard;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Shardable {

    protected static final List<AvaireShard> SHARDS = new CopyOnWriteArrayList<>();

    public List<AvaireShard> getShards() {
        return SHARDS;
    }

    /**
     * This returns the {@link com.avairebot.shard.AvaireShard Shard} which has the same id as the one provided.
     * <br>If there is not shard enabled with an id that matches the provided one, this returns {@code null}.
     *
     * @param shardId The id of the requested {@link AvaireShard Shard}.
     * @return Possible-null {@link AvaireShard Shard} with matching id.
     */
    public AvaireShard getShardById(int shardId) {
        for (AvaireShard shard : SHARDS) {
            if (shard.getShardId() == shardId) {
                return shard;
            }
        }
        return null;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * @param id The id of the requested {@link net.dv8tion.jda.core.entities.User User}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.User User} with matching id.
     * @throws java.lang.NumberFormatException If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     */
    public User getUserById(String id) {
        for (AvaireShard shard : getShards()) {
            User userById = shard.getJDA().getUserById(id);
            if (userById != null) {
                return userById;
            }
        }
        return null;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.User User} which has the same id as the one provided.
     * <br>If there is no visible user with an id that matches the provided one, this returns {@code null}.
     *
     * @param id The id of the requested {@link net.dv8tion.jda.core.entities.User User}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.User User} with matching id.
     */
    public User getUserById(long id) {
        for (AvaireShard shard : getShards()) {
            User userById = shard.getJDA().getUserById(id);
            if (userById != null) {
                return userById;
            }
        }
        return null;
    }

    /**
     * This unmodifiable returns all {@link net.dv8tion.jda.core.entities.User Users} that have the same username as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.User Users} with the provided name, then this returns an empty list.
     * <p>
     * <p><b>Note: </b> This does **not** consider nicknames, it only considers {@link net.dv8tion.jda.core.entities.User#getName()}
     *
     * @param name       The name of the requested {@link net.dv8tion.jda.core.entities.User Users}.
     * @param ignoreCase Whether to ignore case or not when comparing the provided name to each {@link net.dv8tion.jda.core.entities.User#getName()}.
     * @return Possibly-empty list of {@link net.dv8tion.jda.core.entities.User Users} that all have the same name as the provided name.
     */
    public List<User> getUsersByName(String name, boolean ignoreCase) {
        List<User> users = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            users.addAll(shard.getJDA().getUsersByName(name, ignoreCase));
        }
        return users;
    }

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Guild Guilds} that contain all given users as their members.
     *
     * @param users The users which all the returned {@link net.dv8tion.jda.core.entities.Guild Guilds} must contain.
     * @return Unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guild} instances which have all {@link net.dv8tion.jda.core.entities.User Users} in them.
     */
    public List<Guild> getMutualGuilds(User... users) {
        List<Guild> guilds = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            guilds.addAll(shard.getJDA().getMutualGuilds(users));
        }
        return guilds;
    }

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Guild Guilds} that contain all given users as their members.
     *
     * @param users The users which all the returned {@link net.dv8tion.jda.core.entities.Guild Guilds} must contain.
     * @return Unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guild} instances which have all {@link net.dv8tion.jda.core.entities.User Users} in them.
     */
    public List<Guild> getMutualGuilds(Collection<User> users) {
        List<Guild> guilds = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            guilds.addAll(shard.getJDA().getMutualGuilds(users));
        }
        return guilds;
    }

    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.core.entities.Guild Guilds} that the logged account is connected to.
     * <br>If this account is not connected to any {@link net.dv8tion.jda.core.entities.Guild Guilds}, this will return an empty list.
     * <p>
     * <p>If the developer is sharding ({@link net.dv8tion.jda.core.JDABuilder#useSharding(int, int)}, then this list
     * will only contain the {@link net.dv8tion.jda.core.entities.Guild Guilds} that the shard is actually connected to.
     * Discord determines which guilds a shard is connect to using the following format:
     * <br>Guild connected if shardId == (guildId {@literal >>} 22) % totalShards;
     * <br>Source for formula: <a href="https://discordapp.com/developers/docs/topics/gateway#sharding">Discord Documentation</a>
     *
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.core.entities.Guild Guilds} that this account is connected to.
     */
    public List<Guild> getGuilds() {
        List<Guild> guilds = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            guilds.addAll(shard.getJDA().getGuilds());
        }
        return guilds;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param id The id of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Guild Guild} with matching id.
     * @throws java.lang.NumberFormatException If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     */
    public Guild getGuildById(String id) {
        for (AvaireShard shard : getShards()) {
            Guild guildById = shard.getJDA().getGuildById(id);
            if (guildById != null) {
                return guildById;
            }
        }
        return null;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.Guild Guild} which has the same id as the one provided.
     * <br>If there is no connected guild with an id that matches the provided one, then this returns {@code null}.
     *
     * @param id The id of the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Guild Guild} with matching id.
     */
    public Guild getGuildById(long id) {
        for (AvaireShard shard : getShards()) {
            Guild guildById = shard.getJDA().getGuildById(id);
            if (guildById != null) {
                return guildById;
            }
        }
        return null;
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.Guild Guilds} that have the same name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.Guild Guilds} with the provided name, then this returns an empty list.
     *
     * @param name       The name of the requested {@link net.dv8tion.jda.core.entities.Guild Guilds}.
     * @param ignoreCase Whether to ignore case or not when comparing the provided name to each {@link net.dv8tion.jda.core.entities.Guild#getName()}.
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.core.entities.Guild Guilds} that all have the same name as the provided name.
     */
    public List<Guild> getGuildsByName(String name, boolean ignoreCase) {
        List<Guild> guilds = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            guilds.addAll(shard.getJDA().getGuildsByName(name, ignoreCase));
        }
        return guilds;
    }

    /**
     * All {@link net.dv8tion.jda.core.entities.Role Roles} this JDA instance can see. <br>This will iterate over each
     * {@link net.dv8tion.jda.core.entities.Guild Guild} retrieved from {@link #getGuilds()} and collect its {@link
     * net.dv8tion.jda.core.entities.Guild#getRoles() Guild.getRoles()}.
     *
     * @return Immutable List of all visible Roles
     */
    public List<Role> getRoles() {
        List<Role> roles = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            roles.addAll(shard.getJDA().getRoles());
        }
        return roles;
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.core.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link net.dv8tion.jda.core.entities.Guild Guilds} and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param id The id of the searched Role
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Role Role} for the specified ID
     * @throws java.lang.NumberFormatException If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     */
    public Role getRoleById(String id) {
        for (AvaireShard shard : getShards()) {
            Role roleById = shard.getJDA().getRoleById(id);
            if (roleById != null) {
                return roleById;
            }
        }
        return null;
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.core.entities.Role Role} associated to the provided id. <br>This iterates
     * over all {@link net.dv8tion.jda.core.entities.Guild Guilds} and check whether a Role from that Guild is assigned
     * to the specified ID and will return the first that can be found.
     *
     * @param id The id of the searched Role
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Role Role} for the specified ID
     */
    public Role getRoleById(long id) {
        for (AvaireShard shard : getShards()) {
            Role roleById = shard.getJDA().getRoleById(id);
            if (roleById != null) {
                return roleById;
            }
        }
        return null;
    }

    /**
     * Retrieves all {@link net.dv8tion.jda.core.entities.Role Roles} visible to this JDA instance.
     * <br>This simply filters the Roles returned by {@link #getRoles()} with the provided name, either using
     * {@link String#equals(Object)} or {@link String#equalsIgnoreCase(String)} on {@link net.dv8tion.jda.core.entities.Role#getName()}.
     *
     * @param name       The name for the Roles
     * @param ignoreCase Whether to use {@link String#equalsIgnoreCase(String)}
     * @return Immutable List of all Roles matching the parameters provided.
     */
    public List<Role> getRolesByName(String name, boolean ignoreCase) {
        List<Role> roles = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            roles.addAll(shard.getJDA().getRolesByName(name, ignoreCase));
        }
        return roles;
    }

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Category Category} that matches the provided id. <br>If there is no
     * matching {@link net.dv8tion.jda.core.entities.Category Category} this returns {@code null}.
     *
     * @param id The snowflake ID of the wanted Category
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Category Category} for the provided ID.
     * @throws java.lang.IllegalArgumentException If the provided ID is not a valid {@code long}
     */
    public Category getCategoryById(String id) {
        for (AvaireShard shard : getShards()) {
            Category categoryById = shard.getJDA().getCategoryById(id);
            if (categoryById != null) {
                return categoryById;
            }
        }
        return null;
    }

    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Category Category} that matches the provided id. <br>If there is no
     * matching {@link net.dv8tion.jda.core.entities.Category Category} this returns {@code null}.
     *
     * @param id The snowflake ID of the wanted Category
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Category Category} for the provided ID.
     */
    public Category getCategoryById(long id) {
        for (AvaireShard shard : getShards()) {
            Category categoryById = shard.getJDA().getCategoryById(id);
            if (categoryById != null) {
                return categoryById;
            }
        }
        return null;
    }

    /**
     * Gets all {@link net.dv8tion.jda.core.entities.Category Categories} visible to the currently logged in account.
     *
     * @return An immutable list of all visible {@link net.dv8tion.jda.core.entities.Category Categories}.
     */
    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            categories.addAll(shard.getJDA().getCategories());
        }
        return categories;
    }

    /**
     * Gets a list of all {@link net.dv8tion.jda.core.entities.Category Categories} that have the same name as the one
     * provided. <br>If there are no matching categories this will return an empty list.
     *
     * @param name       The name to check
     * @param ignoreCase Whether to ignore case on name checking
     * @return Immutable list of all categories matching the provided name
     * @throws java.lang.IllegalArgumentException If the provided name is {@code null}
     */
    public List<Category> getCategoriesByName(String name, boolean ignoreCase) {
        List<Category> categories = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            categories.addAll(shard.getJDA().getCategoriesByName(name, ignoreCase));
        }
        return categories;
    }

    /**
     * An unmodifiable List of all {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} of all connected
     * {@link net.dv8tion.jda.core.entities.Guild Guilds}.
     * <p>
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present in this list does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is possible that you will see fewer channels than this returns. This is because the discord
     * client hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @return Possibly-empty list of all known {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
     */
    public List<TextChannel> getTextChannels() {
        List<TextChannel> textChannels = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            textChannels.addAll(shard.getJDA().getTextChannels());
        }
        return textChannels;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with an id that matches the
     * provided one, then this returns {@code null}.
     * <p>
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is you will not see the channel that this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @param id The id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with matching id.
     * @throws java.lang.NumberFormatException If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     */
    public TextChannel getTextChannelById(String id) {
        for (AvaireShard shard : getShards()) {
            TextChannel textChannelById = shard.getJDA().getTextChannelById(id);
            if (textChannelById != null) {
                return textChannelById;
            }
        }
        return null;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with an id that matches the
     * provided one, then this returns {@code null}.
     * <p>
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is you will not see the channel that this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @param id The id of the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with matching id.
     */
    public TextChannel getTextChannelById(long id) {
        for (AvaireShard shard : getShards()) {
            TextChannel textChannelById = shard.getJDA().getTextChannelById(id);
            if (textChannelById != null) {
                return textChannelById;
            }
        }
        return null;
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} that have the same name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} with the provided name, then this returns an empty list.
     * <p>
     * <p><b>Note:</b> just because a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} is present in this list does
     * not mean that you will be able to send messages to it. Furthermore, if you log into this account on the discord
     * client, it is possible that you will see fewer channels than this returns. This is because the discord client
     * hides any {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that you don't have the
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permission in.
     *
     * @param name       The name of the requested {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}.
     * @param ignoreCase Whether to ignore case or not when comparing the provided name to each {@link net.dv8tion.jda.core.entities.TextChannel#getName()}.
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} that all have the
     * same name as the provided name.
     */
    public List<TextChannel> getTextChannelsByName(String name, boolean ignoreCase) {
        List<TextChannel> textChannels = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            textChannels.addAll(shard.getJDA().getTextChannelsByName(name, ignoreCase));
        }
        return textChannels;
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} of all connected
     * {@link net.dv8tion.jda.core.entities.Guild Guilds}.
     *
     * @return Possible-empty list of all known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     */
    public List<VoiceChannel> getVoiceChannels() {
        List<VoiceChannel> voiceChannels = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            voiceChannels.addAll(shard.getJDA().getVoiceChannels());
        }
        return voiceChannels;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param id The id of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with matching id.
     * @throws java.lang.NumberFormatException If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     */
    public VoiceChannel getVoiceChannelById(String id) {
        for (AvaireShard shard : getShards()) {
            VoiceChannel voiceChannelById = shard.getJDA().getVoiceChannelById(id);
            if (voiceChannelById != null) {
                return voiceChannelById;
            }
        }
        return null;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param id The id of the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with matching id.
     */
    public VoiceChannel getVoiceChannelById(long id) {
        for (AvaireShard shard : getShards()) {
            VoiceChannel voiceChannelById = shard.getJDA().getVoiceChannelById(id);
            if (voiceChannelById != null) {
                return voiceChannelById;
            }
        }
        return null;
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} that have the same name as the one provided.
     * <br>If there are no {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} with the provided name, then this returns an empty list.
     *
     * @param name       The name of the requested {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}.
     * @param ignoreCase Whether to ignore case or not when comparing the provided name to each {@link net.dv8tion.jda.core.entities.VoiceChannel#getName()}.
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} that all have the
     * same name as the provided name.
     */
    public List<VoiceChannel> getVoiceChannelByName(String name, boolean ignoreCase) {
        List<VoiceChannel> voiceChannels = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            voiceChannels.addAll(shard.getJDA().getVoiceChannelByName(name, ignoreCase));
        }
        return voiceChannels;
    }

    /**
     * An unmodifiable list of all known {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels}.
     *
     * @return Possibly-empty list of all {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannels}.
     */
    public List<PrivateChannel> getPrivateChannels() {
        List<PrivateChannel> privateChannels = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            privateChannels.addAll(shard.getJDA().getPrivateChannels());
        }
        return privateChannels;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param id The id of the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} with matching id.
     * @throws java.lang.NumberFormatException If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     */
    public PrivateChannel getPrivateChannelById(String id) {
        for (AvaireShard shard : getShards()) {
            PrivateChannel privateChannelById = shard.getJDA().getPrivateChannelById(id);
            if (privateChannelById != null) {
                return privateChannelById;
            }
        }
        return null;
    }

    /**
     * This returns the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} which has the same id as the one provided.
     * <br>If there is no known {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} with an id that matches the provided
     * one, then this returns {@code null}.
     *
     * @param id The id of the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} with matching id.
     */
    public PrivateChannel getPrivateChannelById(long id) {
        for (AvaireShard shard : getShards()) {
            PrivateChannel privateChannelById = shard.getJDA().getPrivateChannelById(id);
            if (privateChannelById != null) {
                return privateChannelById;
            }
        }
        return null;
    }

    /**
     * A collection of all to us known emotes (managed/restricted included).
     * <p>
     * <p><b>Hint</b>: To check whether you can use an {@link net.dv8tion.jda.core.entities.Emote Emote} in a specific
     * context you can use {@link Emote#canInteract(net.dv8tion.jda.core.entities.Member)} or {@link
     * Emote#canInteract(net.dv8tion.jda.core.entities.User, net.dv8tion.jda.core.entities.MessageChannel)}
     * <p>
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</b>
     *
     * @return An immutable list of Emotes (which may or may not be available to usage).
     */
    public List<Emote> getEmotes() {
        List<Emote> emotes = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            emotes.addAll(shard.getJDA().getEmotes());
        }
        return emotes;
    }

    /**
     * Retrieves an emote matching the specified {@code id} if one is available in our cache.
     * <p>
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</b>
     *
     * @param id The id of the requested {@link net.dv8tion.jda.core.entities.Emote}.
     * @return An {@link net.dv8tion.jda.core.entities.Emote Emote} represented by this id or null if none is found in
     * our cache.
     * @throws java.lang.NumberFormatException If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     */
    public Emote getEmoteById(String id) {
        for (AvaireShard shard : getShards()) {
            Emote emoteById = shard.getJDA().getEmoteById(id);
            if (emoteById != null) {
                return emoteById;
            }
        }
        return null;
    }

    /**
     * Retrieves an emote matching the specified {@code id} if one is available in our cache.
     * <p>
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</b>
     *
     * @param id The id of the requested {@link net.dv8tion.jda.core.entities.Emote}.
     * @return An {@link net.dv8tion.jda.core.entities.Emote Emote} represented by this id or null if none is found in
     * our cache.
     */
    public Emote getEmoteById(long id) {
        for (AvaireShard shard : getShards()) {
            Emote emoteById = shard.getJDA().getEmoteById(id);
            if (emoteById != null) {
                return emoteById;
            }
        }
        return null;
    }

    /**
     * An unmodifiable list of all {@link net.dv8tion.jda.core.entities.Emote Emotes} that have the same name as the one
     * provided. <br>If there are no {@link net.dv8tion.jda.core.entities.Emote Emotes} with the provided name, then
     * this returns an empty list.
     * <p>
     * <p><b>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</b>
     *
     * @param name       The name of the requested {@link net.dv8tion.jda.core.entities.Emote Emotes}.
     * @param ignoreCase Whether to ignore case or not when comparing the provided name to each {@link
     *                   net.dv8tion.jda.core.entities.Emote#getName()}.
     * @return Possibly-empty list of all the {@link net.dv8tion.jda.core.entities.Emote Emotes} that all have the same
     * name as the provided name.
     */
    public List<Emote> getEmotesByName(String name, boolean ignoreCase) {
        List<Emote> emotes = new ArrayList<>();
        for (AvaireShard shard : getShards()) {
            emotes.addAll(shard.getJDA().getEmotesByName(name, ignoreCase));
        }
        return emotes;
    }

    /**
     * Checks if we're ready yet by checking if all the shards are connected and ready to serve events.
     *
     * @return <code>True</code> if all shards has connected and are ready, <code>False</code> otherwise.
     */
    public boolean areWeReadyYet() {
        for (AvaireShard shard : getShards()) {
            if (shard.getJDA().getStatus() != JDA.Status.CONNECTED) {
                return false;
            }
        }
        return true;
    }
}
