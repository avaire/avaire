package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.BanableCommand;
import com.avairebot.contracts.commands.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SoftBanCommand extends BanableCommand {

    public SoftBanCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Soft Ban Command";
    }

    @Override
    public String getDescription() {
        return "Bans the mentioned user from the server with the provided reason without removing any of the messages they have sent, this action will be reported to any channel that has modloging enabled.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <user> [reason]` - Bans the mentioned user with the given reason.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command @Senither Being a potato`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(BanCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("softban", "sban");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("require:all,general.ban_members");
    }

    @Override
    public boolean onCommand(CommandMessage message, String[] args) {
        return ban(avaire, this, message, args, true);
    }
}
