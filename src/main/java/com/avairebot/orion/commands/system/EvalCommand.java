package com.avairebot.orion.commands.system;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.SystemCommand;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EvalCommand extends SystemCommand {

    public EvalCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Eval Command";
    }

    @Override
    public String getDescription() {
        return "Evaluates and executes code.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <code>` - Evaluates and executes the given code.");
    }

    @Override
    public String getExampleUsage() {
        return "`:command 5*(5+9)`";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("eval");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            MessageFactory.makeWarning(message, "No arguments given, there are nothing to evaluate.").queue();
            return false;
        }

        String[] rawArguments = message.getRawContent().split(" ");
        String evalMessage = String.join(" ", Arrays.copyOfRange(rawArguments, 1, rawArguments.length));

        try {
            Object out = createScriptEngine(message).eval("(function() { with (imports) { return " + evalMessage + "}})();");
            String output = out == null ? "Executed without error, void was returned so there is nothing to show." : out.toString();

            if (output.length() > 1890) {
                output = output.substring(0, 1890) + "...";
            }

            message.getChannel().sendMessage("```xl\n" + output + "```").queue();
        } catch (ScriptException e) {
            message.getChannel().sendMessage("**Error:**\n```xl\n" + e.toString() + "```").queue();
        }

        return true;
    }

    private ScriptEngine createScriptEngine(Message message) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");

        engine.put("message", message);
        engine.put("channel", message.getChannel());
        engine.put("jda", message.getJDA());
        engine.put("orion", orion);

        if (message.isFromType(ChannelType.TEXT)) {
            engine.put("guild", message.getGuild());
            engine.put("member", message.getMember());
        }

        engine.eval("var imports = new JavaImporter(" +
                "java.io," +
                "java.lang," +
                "java.util," +
                "Packages.net.dv8tion.jda.core," +
                "Packages.net.dv8tion.jda.core.entities," +
                "Packages.net.dv8tion.jda.core.entities.impl," +
                "Packages.net.dv8tion.jda.core.managers," +
                "Packages.net.dv8tion.jda.core.managers.impl," +
                "Packages.net.dv8tion.jda.core.utils);");

        return engine;
    }
}
