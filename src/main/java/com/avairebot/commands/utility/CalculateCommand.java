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

package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.udojava.evalex.Expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CalculateCommand extends Command {

    public CalculateCommand(AvaIre avaire) {
        super(avaire);
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
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command (-50 + sqrt(50 ^ 2 - ((4 * 5) * (100 - 955)))) / (2 * 5) == 9`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("calculate", "calc");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "equation");
        }

        String string = String.join(" ", args).trim();

        try {
            Expression expression = createExpression(string);
            BigDecimal result = expression.eval(false);

            if (expression.isBoolean()) {
                context.makeInfo(
                    generateEasterEgg(context, expression, result, string, result.intValueExact() == 1
                        ? context.i18n("boolean.true") : context.i18n("boolean.false"))
                ).queue();
                return true;
            }

            context.makeInfo(generateEasterEgg(context, expression, result, string, result.toPlainString())).queue();
        } catch (Exception ex) {
            return sendErrorMessage(context, ex.getMessage().replaceAll("'", "`"));
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

        expression.setPrecision(128);
        expression.setRoundingMode(RoundingMode.UNNECESSARY);

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

    private String generateEasterEgg(CommandMessage context, Expression expression, BigDecimal result, String query, String stringifiedResult) {
        if (stringifiedResult.equals("69")) {
            return stringifiedResult + "\t( ͡° ͜ʖ ͡°)";
        }

        query = query.replaceAll(" ", "");

        if (query.startsWith("2+2-1") && ((expression.isBoolean() && result.intValueExact() == 1) || result.intValueExact() == 3)) {
            return stringifiedResult + context.i18n("eastereggs.quickMaths");
        }

        if (query.equals("10") && stringifiedResult.equals("10")) {
            return context.i18n("eastereggs.binary");
        }

        return stringifiedResult;
    }
}
