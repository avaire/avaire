package com.avairebot.orion.commands.administration;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.modules.BanModule;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SoftBanCommand extends Command {

    public SoftBanCommand(Orion orion) {
        super(orion, false);
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
    public List<String> getTriggers() {
        return Arrays.asList("softban", "sban");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("require:all,general.ban_members");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        return BanModule.ban(this, message, args, true);
    }
}
