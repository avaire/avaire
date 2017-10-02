package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.Statistics;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RipCommand extends Command {

    public RipCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "RIP Command";
    }

    @Override
    public String getDescription() {
        return "Pay your respects";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Pay your respects");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("rip");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList("throttle:user,1,5");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        Statistics.addRespects();

        try {
            orion.database.newQueryBuilder(Constants.STATISTICS_TABLE_NAME)
                    .update(statement -> statement.setRaw("respects", "`respects` + 1"));
        } catch (SQLException ex) {
            return false;
        }

        EmbedBuilder embed = MessageFactory.createEmbeddedBuilder()
                .setColor(Color.decode("#2A2C31"))
                .setDescription(String.format("**%s** has paid their respects.", message.getMember().getEffectiveName()))
                .setFooter(String.format("%s Today, %s Overall",
                        Statistics.getRespects(), getTotalRespects()
                ), null);

        message.getChannel().sendMessage(embed.build()).queue();
        return true;
    }

    private int getTotalRespects() {
        try {
            return orion.database.newQueryBuilder(Constants.STATISTICS_TABLE_NAME).get().first()
                    .getInt("respects", Statistics.getRespects()) + 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
