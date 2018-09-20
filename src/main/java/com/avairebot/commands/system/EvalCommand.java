/*
 * Copyright (c) 2018.
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
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.SystemCommand;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class EvalCommand extends SystemCommand {

    private static final Logger log = LoggerFactory.getLogger(EvalCommand.class);

    @Nullable
    private Future lastTask;
    private ScriptEngine engine;

    public EvalCommand(AvaIre avaire) {
        super(avaire);

        engine = new ScriptEngineManager()
            .getEngineByName("nashorn");

        try {
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
                "Packages.com.avairebot.language," +
                "Packages.com.avairebot.metrics," +
                "Packages.com.avairebot.logger," +
                "Packages.com.avairebot.cache," +
                "Packages.com.avairebot.audio," +
                "Packages.com.avairebot.time);");
        } catch (ScriptException e) {
            log.error("Failed to init eval command", e);
        }
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
        return Arrays.asList(
            "`:command <code>` - Evaluates and executes the given code.",
            "`:command <kill|-k>` - Kills the last task if it is still running.",
            "`:command <timeout|-t> <timeout lenght> <code>` - Evaluates and executes the given code with the given timeout."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command context.makeInfo(\"Hello, World\").queue();`",
            "`:command -t 10 return \"Some Code\"`"
        );
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

        if (args.length == 1 && (args[0].equals("kill") || args[0].equals("-k"))) {
            return killLastTask(context);
        }

        final long started = System.currentTimeMillis();

        context.getMessageChannel().sendTyping().queue();
        int timeout = args[0].equals("timeout") || args[0].equals("-t")
            ? NumberUtil.parseInt(args[1], -1) : -1;

        String[] parts = context.getMessage().getContentRaw().split(" ");
        final String source = String.join(" ", Arrays.copyOfRange(
            parts, calculateSourceLength(context, timeout), parts.length
        ));

        engine.put("context", context);
        engine.put("message", context.getMessage());
        engine.put("channel", context.getChannel());
        engine.put("jda", context.getJDA());
        engine.put("avaire", avaire);

        if (context.getMessage().isFromType(ChannelType.TEXT)) {
            engine.put("guild", context.getGuild());
            engine.put("member", context.getMember());
        }

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Eval command execution"));

        Future<?> future = service.submit(() -> {
            Object out;
            try {
                out = engine.eval(
                    "(function() {"
                        + "with (imports) {\n" + source + "\n}"
                        + "})();");

                AvaIre.getLogger().debug("Eval output: {}", out == null ? "NULL" : out.toString());
                String output = out == null ? ":thumbsup::skin-tone-3:" : "```\n" + out.toString() + "\n```";

                context.getMessageChannel().sendMessage(String.format("**Input** ```java\n%s```\n**Output**\n%s\nEval took _%sms_",
                    source, output, System.currentTimeMillis() - started
                )).queue();
            } catch (Exception ex) {
                log.debug("Failed to execute eval command, error: {}", ex.getMessage(), ex);

                context.getChannel().sendMessage(String.format("**Input** ```java\n%s```\n**Error Output**\n```%s```\nEval took _%sms_",
                    source, ex.getMessage(), System.currentTimeMillis() - started)
                ).queue();
            }
        });
        this.lastTask = future;

        Thread script = new Thread("Eval comm waiter") {
            @Override
            public void run() {
                try {
                    if (timeout > -1) {
                        future.get(timeout, TimeUnit.SECONDS);
                    }
                } catch (final TimeoutException ex) {
                    future.cancel(true);
                    context.makeWarning("Task exceeded time limit of " + timeout + " seconds.").queue();
                } catch (final Exception ex) {
                    context.makeError(String.format("`%s`\n\n`%sms`",
                        ex.getMessage(), System.currentTimeMillis() - started)
                    ).queue();
                }
            }
        };
        script.start();

        return true;
    }

    private boolean killLastTask(CommandMessage context) {
        if (lastTask == null) {
            context.makeWarning("No task found to kill.").queue();
            return false;
        }

        if (lastTask.isDone() || lastTask.isCancelled()) {
            context.makeWarning("Task isn't running.").queue();
            return false;
        }

        lastTask.cancel(true);
        context.makeSuccess("Task has been killed.").queue();

        return true;
    }

    private int calculateSourceLength(CommandMessage context, int timeout) {
        int sourceLength = context.isMentionableCommand() ? 2 : 1;
        if (timeout > 0) {
            sourceLength += 2;
        }
        return sourceLength;
    }
}
