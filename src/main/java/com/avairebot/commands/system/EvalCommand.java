package com.avairebot.commands.system;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import net.dv8tion.jda.core.entities.ChannelType;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.List;

public class EvalCommand extends SystemCommand {

    public EvalCommand(AvaIre avaire) {
        super(avaire);
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
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 5*(5+9)`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("eval");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            context.makeWarning("No arguments given, there are nothing to evaluate.").queue();
            return false;
        }

        try {
            Object out = createScriptEngine(context).eval("(function() { with (imports) {\n\t" + context.getContentRaw() + "\n}})();");
            String output = out == null ? "Executed without error, void was returned so there is nothing to show." : out.toString();

            if (output.length() > 1890) {
                output = output.substring(0, 1890) + "...";
            }

            context.getMessageChannel().sendMessage("```xl\n" + output + "```").queue();
        } catch (ScriptException e) {
            context.getMessageChannel().sendMessage("**Error:**\n```xl\n" + e.toString() + "```").queue();
        }

        return true;
    }

    private ScriptEngine createScriptEngine(CommandMessage context) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");

        engine.put("context", context);
        engine.put("message", context.getMessage());
        engine.put("channel", context.getChannel());
        engine.put("jda", context.getJDA());
        engine.put("avaire", avaire);

        if (context.getMessage().isFromType(ChannelType.TEXT)) {
            engine.put("guild", context.getGuild());
            engine.put("member", context.getMember());
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
            "Packages.net.dv8tion.jda.core.utils," +
            "Packages.com.avairebot.database.controllers," +
            "Packages.com.avairebot.permissions," +
            "Packages.com.avairebot.utilities," +
            "Packages.com.avairebot.factories," +
            "Packages.com.avairebot.logger," +
            "Packages.com.avairebot.cache," +
            "Packages.com.avairebot.audio," +
            "Packages.com.avairebot.time);");

        return engine;
    }
}
