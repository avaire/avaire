package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.audio.LavalinkManager;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.SystemCommand;
import lavalink.client.io.LavalinkLoadBalancer;
import lavalink.client.io.LavalinkSocket;
import lavalink.client.io.RemoteStats;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LavalinkCommand extends SystemCommand {

    private static final DecimalFormat percentageFormat = new DecimalFormat("###.##");

    public LavalinkCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Lavalink Command";
    }

    @Override
    public String getDescription() {
        return "This command can be used to list the status of Lavalink nodes, adding, and removing nodes on the fly during runtime.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command list` - List all Lavalink nodes",
            "`:command show <node>` - Shows in-depth information about the node",
            "`:command remove <name> ` - Removes the node from Lavalink",
            "`:command add <name> <url> <pass>` - Adds the node to Lavalink"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("lavalink");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()
            ? CommandPriority.SYSTEM : CommandPriority.HIDDEN;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (!LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
            return false;
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "You must specify what command you want to use.");
        }

        switch (args[0].toLowerCase()) {
            case "add":
                return addNode(context, args);

            case "remove":
                return removeNode(context, args);

            case "show":
            case "info":
                return showNode(context, args);

            case "list":
            default:
                return listNodes(context);
        }
    }

    private boolean addNode(CommandMessage context, String[] args) {
        if (args.length < 3) {
            return sendErrorMessage(context, "The node name, uri, and password is required!");
        }

        String name = args[1];

        URI uri;
        try {
            uri = new URI(args[2]);
        } catch (URISyntaxException e) {
            return sendErrorMessage(context, args[2] + " is not a valid URI");
        }

        String password = args[3];

        LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().addNode(name, uri, password);

        context.makeSuccess("Added node: :name @ :uri")
            .set("name", name)
            .set("uri", uri)
            .queue();

        return true;
    }

    private boolean removeNode(CommandMessage context, String[] args) {
        if (args.length == 1) {
            return sendErrorMessage(context, "You must include the name of the node you want to view information for.");
        }

        String nodeName = args[1];
        List<LavalinkSocket> nodes = LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().getNodes();

        int key = -1;
        for (int i = 0; i < nodes.size(); i++) {
            LavalinkSocket node = nodes.get(i);
            if (node.getName().equals(nodeName)) {
                key = i;
                break;
            }
        }

        if (key < 0) {
            return sendErrorMessage(context, "No nodes was found with the name: " + nodeName);
        }

        LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().removeNode(key);

        context.makeSuccess(":node has been removed from the Lavalink runtime.")
            .set("node", nodeName)
            .queue();

        return true;
    }

    private boolean showNode(CommandMessage context, String[] args) {
        if (LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().getNodes().isEmpty()) {
            return sendErrorMessage(context, "There are no remote lavalink nodes registered.");
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "You must include the name of the node you want to view information for.");
        }

        String nodeName = args[1];
        List<LavalinkSocket> nodes = LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().getNodes().stream()
            .filter(ll -> ll.getName().equals(nodeName))
            .collect(Collectors.toList());

        if (nodes.isEmpty()) {
            return sendErrorMessage(context, "No nodes was found with the name: " + nodeName);
        }

        LavalinkSocket socket = nodes.get(0);

        RemoteStats stats = socket.getStats();


        List<String> messages = new ArrayList<>();

        messages.add("Name:                    " + socket.getName());
        messages.add("Host:                    " + (context.getGuild() == null ? socket.getRemoteUri() : "--Redacted--"));

        if (stats == null) {
            messages.add("\nNo stats have been received from this node! Is the node down?");
        } else {
            messages.add("Playing players:         " + stats.getPlayingPlayers());
            messages.add("Lavalink load:           " + formatPercent(stats.getLavalinkLoad()));
            messages.add("System load:             " + formatPercent(stats.getSystemLoad()));
            messages.add("Memory:                  " + stats.getMemUsed() / 1000000 + "MB / " + stats.getMemReservable() / 1000000 + "MB");
            messages.add("---------------");
            messages.add("Average frames sent:     " + stats.getAvgFramesSentPerMinute());
            messages.add("Average frames nulled:   " + stats.getAvgFramesNulledPerMinute());
            messages.add("Average frames deficit:  " + stats.getAvgFramesDeficitPerMinute());
            messages.add("---------------");

            LavalinkLoadBalancer.Penalties penalties = LavalinkLoadBalancer.getPenalties(socket);
            messages.add("Penalties Total:         " + penalties.getTotal());
            messages.add("Player Penalty:          " + penalties.getPlayerPenalty());
            messages.add("CPU Penalty:             " + penalties.getCpuPenalty());
            messages.add("Deficit Frame Penalty:   " + penalties.getDeficitFramePenalty());
            messages.add("Null Frame Penalty:      " + penalties.getNullFramePenalty());
            messages.add("Raw: " + penalties.toString());
        }

        context.getMessageChannel()
            .sendMessage("```\n" + String.join("\n", messages) + "```")
            .queue();

        return true;
    }

    private boolean listNodes(CommandMessage context) {
        List<String> nodes = new ArrayList<>();
        for (LavalinkSocket socket : LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink().getNodes()) {
            nodes.add("- " + socket.getName());
            nodes.add("\t*Status:* " + (socket.isAvailable() ? "Connected" : "Disconnected"));
        }

        if (nodes.isEmpty()) {
            context.makeInfo("There are no active nodes available").queue();
            return true;
        }

        context.makeInfo(String.join("\n", nodes))
            .setTitle("Lavalink Nodes (" + (nodes.size() / 2) + ")")
            .queue();

        return true;
    }

    private String roundToTwo(double value) {
        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return percentageFormat.format((double) tmp / factor);
    }

    private String formatPercent(double percent) {
        return roundToTwo(percent * 100) + "%";
    }
}
