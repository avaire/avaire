/*
 * Copyright (c) 2019.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.chat.SimplePaginator;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.contracts.commands.sort.ServerComparable;
import com.avairebot.time.Carbon;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Guild;

import java.util.*;

public class ServersCommand extends SystemCommand {

    public ServersCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Servers Command";
    }

    @Override
    public String getDescription() {
        return "Lists servers the bot is in with some basic information about them, allowing admins to list servers using different filters.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command filters` - Lists the different filters that are available.",
            "`:command list <filter> [page]` - Displays a list of servers using the given filter.",
            "`:command show <id>` - Shows information about the server with the given ID."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command list users` - Lists servers using the users filter.",
            "`:command show 284083636368834561` - Shows information about the server with the given ID."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("servers", "server");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.SYSTEM_ROLE;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "action");
        }

        switch (args[0].toLowerCase()) {
            case "filter":
            case "filters":
                return handleFilters(context);

            case "show":
            case "display":
                return handleShow(context, Arrays.copyOfRange(args, 1, args.length));

            case "list":
                return handleList(context, Arrays.copyOfRange(args, 1, args.length));
        }

        return sendErrorMessage(context, "Invalid action given, you must either use `filter`, `show`, or `list` to use the command.");
    }

    private boolean handleFilters(CommandMessage context) {
        PlaceholderMessage message = context.makeInfo("Below you'll find a few different filters that can be used to sort the servers differently.")
            .setTitle("Filter Types");

        for (SortTypes sortType : SortTypes.values()) {
            message.addField(sortType.getTriggers().get(0), sortType.getDescription(), false);
        }

        message.queue();

        return true;
    }

    private boolean handleShow(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "id");
        }

        try {
            long id = Long.parseLong(args[0]);

            Guild guild = avaire.getShardManager().getGuildById(id);
            if (guild == null) {
                return sendErrorMessage(context, "No guilds found with an ID of `{0}`", args[0]);
            }

            Carbon time = Carbon.createFromOffsetDateTime(guild.getCreationTime());

            context.makeInfo(guild.getId())
                .setTitle(guild.getName())
                .setThumbnail(guild.getIconUrl())
                .addField("Owner", guild.getOwner().getUser().getName() + "#" + guild.getOwner().getUser().getDiscriminator(), true)
                .addField("Owner ID", guild.getOwner().getUser().getId(), true)
                .addField("Text Channels", NumberUtil.formatNicely(guild.getTextChannels().size()), true)
                .addField("Voice Channels", NumberUtil.formatNicely(guild.getVoiceChannels().size()), true)
                .addField("Members", NumberUtil.formatNicely(guild.getMembers().size()), true)
                .addField("Roles", NumberUtil.formatNicely(guild.getRoles().size()), true)
                .addField("Region", guild.getRegion().getName(), true)
                .addField("Created At", time.toDayDateTimeString() + "\n*About " + time.diffForHumans() + "*", true)
                .queue();
        } catch (NumberFormatException e) {
            return sendErrorMessage(context, "Invalid guild ID given, `{0}` is not a valid ID.", args[0]);
        }

        return true;
    }

    private boolean handleList(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "filter");
        }

        SortTypes sortType = SortTypes.fromTrigger(args[0]);
        if (sortType == null) {
            return sendErrorMessage(context, "Invalid filter given, you must provide a valid filter to see a list of servers.");
        }

        List<Server> servers = new ArrayList<>();
        for (Guild guild : avaire.getShardManager().getGuilds()) {
            servers.add(new Server(guild));
        }

        sortType.sort(servers);
        List<String> messages = new ArrayList<>();
        SimplePaginator<Server> paginator = new SimplePaginator<>(servers, 5);
        if (args.length > 1) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[1], 1));
        }

        messages.add("```");
        paginator.forEach((index, key, server) -> {
            messages.add(String.format("Rank #%s\n%s (ID: %s)\n\t%s Members with %s Users and %s bots.\n",
                index + 1,
                server.getName(),
                server.getId(),
                NumberUtil.formatNicely(server.getMembers()),
                NumberUtil.formatNicely(server.getUsers()),
                NumberUtil.formatNicely(server.getBots())
            ));
        });
        messages.add("```");

        messages.add("\n" + paginator.generateFooter(
            context.getGuild(),
            generateCommandTrigger(context.getMessage()) + " list " + sortType.getTriggers().get(0)
        ));

        context.makeInfo(String.join("\n", messages))
            .setTitle(String.format("Servers Listed by %s Filter (%s)",
                sortType.getTriggers().get(0),
                NumberUtil.formatNicely(paginator.getTotal())
            )).queue();

        return true;
    }

    enum SortTypes {

        BOTS(
            servers -> servers.sort(Collections.reverseOrder(Comparator.comparingInt(ServersCommand.Server::getBots))),
            "Orders servers by bot count, this doesn't include bots in the server.",
            "bots", "bot"
        ),
        USERS(
            servers -> servers.sort(Collections.reverseOrder(Comparator.comparingInt(ServersCommand.Server::getUsers))),
            "Orders servers by user count, this doesn't include bots in the server.",
            "users", "user"
        ),
        MEMBERS(
            servers -> servers.sort(Collections.reverseOrder(Comparator.comparingInt(ServersCommand.Server::getMembers))),
            "Orders servers by member count, this includes users and bots.",
            "members", "member"
        ),
        NAME(
            servers -> servers.sort(Comparator.comparing(ServersCommand.Server::getName)),
            "Lists servers by their name, in alphabetic order.",
            "names", "name"
        );

        private final ServerComparable comparable;
        private final String description;
        private final List<String> triggers;

        SortTypes(ServerComparable comparable, String description, String... triggers) {
            this.comparable = comparable;
            this.description = description;
            this.triggers = Arrays.asList(triggers);
        }

        public static SortTypes fromTrigger(String name) {
            for (SortTypes types : values()) {
                for (String trigger : types.getTriggers()) {
                    if (trigger.equals(name)) {
                        return types;
                    }
                }
            }
            return null;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getTriggers() {
            return triggers;
        }

        public void sort(List<ServersCommand.Server> servers) {
            comparable.sort(servers);
        }
    }

    public class Server {

        private final String name;

        private final long id;
        private final int members;
        private final int users;
        private final int bots;

        public Server(Guild guild) {
            this.name = guild.getName();

            this.id = guild.getIdLong();
            this.members = guild.getMembers().size();
            this.users = Math.toIntExact(guild.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .count());
            this.bots = members - users;
        }

        public String getName() {
            return name;
        }

        public long getId() {
            return id;
        }

        public int getMembers() {
            return members;
        }

        public int getUsers() {
            return users;
        }

        public int getBots() {
            return bots;
        }
    }
}
