package com.avairebot.orion.commands.utility;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.udojava.evalex.Expression;
import net.dv8tion.jda.core.entities.Message;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CalculateCommand extends Command {

    private static final Pattern NUMBER_REGEX = Pattern.compile("^\\d+$");

    public CalculateCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Calculate Command";
    }

    @Override
    public String getDescription() {
        return "Calculates the given math equations and returns the result for you.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <equation>` - Calculates the result of the given math equation.");
    }

    @Override
    public String getExampleUsage() {
        return "`:command (-50 + sqrt(50 ^ 2 - ((4 * 5) * (100 - 955)))) / (2 * 5) == 9`";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("calculate", "calc");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing argument, the `equation` argument is required!");
        }

        String string = String.join(" ", args).trim();
        if (NUMBER_REGEX.matcher(string).find()) {
            MessageFactory.makeInfo(message, string).queue();
            return true;
        }

        try {
            Expression expression = createExpression(string);
            BigDecimal result = expression.eval();

            System.out.println(expression.getExpression());
            if (expression.isBoolean()) {

                MessageFactory.makeInfo(message, result.intValueExact() == 1
                    ? "True" : "False"
                ).queue();
                return true;
            }

            MessageFactory.makeInfo(message, result.toPlainString()).queue();
        } catch (Exception ex) {
            return sendErrorMessage(message, ex.getMessage().replaceAll("'", "`"));
        }
        return true;
    }

    private Expression createExpression(String string) {
        int where = string.toLowerCase().indexOf("where");

        if (where == -1) {
            return new Expression(string)
                .setVariable("tau", new BigDecimal(Math.PI * 2));
        }

        Expression expression = new Expression(string.substring(0, where).trim())
            .setVariable("tau", new BigDecimal(Math.PI * 2));

        for (String var : string.substring(where + 5, string.length()).trim().split(" and ")) {
            String[] varArgs = var.split("=");
            if (varArgs.length != 2) {
                varArgs = var.split("is");
                if (varArgs.length != 2) {
                    continue;
                }
            }

            expression.setVariable(varArgs[0].trim(), new BigDecimal(varArgs[1].trim()));
        }

        return expression;
    }
}
