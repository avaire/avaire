package com.avairebot.orion.commands.fun;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.AbstractCommand;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;
import com.avairebot.orion.requests.service.ChuckNorrisService;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ChuckNorrisCommand extends AbstractCommand {

    public ChuckNorrisCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Chuck Norris Command";
    }

    @Override
    public String getDescription() {
        return "I will get a random 100% true, real facts about Chuck Norris for you using the \"Internet Chuck Norris Database\".";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`>chucknorris` - Gets a random fact for you");
    }

    @Override
    public String getExampleUsage() {
        return "`>chucknorris`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("chucknorris", "chuck", "norris");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        try {
            RequestFactory.makeGET("http://api.icndb.com/jokes/random")
                    .addParameter("escape", "javascript")
                    .send((Consumer<Response>) response -> {
                        ChuckNorrisService service = (ChuckNorrisService) response.toJson(ChuckNorrisService.class);

                        MessageFactory.makeSuccess(message, service.getValue().getJoke()).queue();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
