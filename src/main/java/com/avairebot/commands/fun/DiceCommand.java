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

package com.avairebot.commands.fun;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.RandomUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class DiceCommand extends Command {

    private final Pattern diceRegEX = Pattern.compile("([0-9]+)d([0-9]+)", Pattern.CASE_INSENSITIVE);

    public DiceCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "Dice Command";
    }

    @Override
    public String getDescription() {
        return "Rolls a dice or multiple dice with the given number of sides.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <dice followed by a D and the sides>` - Rolls some dice randomly");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 4D8 2D4`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("dice");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("missingDice"));
        }

        List<DiceRoll> items = new ArrayList<>();
        int invalidArgument = getInvalidArgument(args);
        if (invalidArgument > -1) {
            return sendErrorMessage(context, context.i18n("invalidDiceFormat", args[invalidArgument]));
        }

        for (int i = 0; i < args.length && i < 5; i++) {
            String arg = args[i];
            String[] split = arg.toLowerCase().split("d");
            int dice, sides;

            try {
                dice = Integer.parseInt(split[0]);
                if (dice < 1 || dice > 36) {
                    return sendErrorMessage(context, context.i18n("diceFormatMustBeGreaterThan", arg));
                }
            } catch (NumberFormatException ex) {
                return sendErrorMessage(context, context.i18n("diceFormatMustBePositive", arg));
            }

            try {
                sides = Integer.parseInt(split[1]);
                if (sides < 1 || sides > 127) {
                    return sendErrorMessage(context, context.i18n("sidesFormatMustBeGreaterThan", arg));
                }
            } catch (NumberFormatException ex) {
                return sendErrorMessage(context, context.i18n("sidesFormatMustBePositive", arg));
            }

            DiceRoll diceRoll = new DiceRoll(arg);

            for (int x = 0; x < dice; x++) {
                diceRoll.addNumber(RandomUtil.getInteger(sides) + 1);
            }
            items.add(diceRoll);
        }

        StringBuilder result = new StringBuilder();
        for (DiceRoll roll : items) {
            result.append(context.i18n("resultedIn",
                roll.getDice(),
                roll.getSum(),
                String.join(" + ", roll.getNumbers())
            )).append("\n");
        }

        context.makeSuccess(String.format("<@%s>%s%s",
            context.getAuthor().getId(),
            getFormatSeparator(context, args),
            result.toString().trim())
        ).queue();
        return true;
    }

    private String getFormatSeparator(CommandMessage context, String[] args) {
        if (args.length == 1) {
            return " ";
        }

        if (args.length > 4) {
            return context.i18n("moreThanFiveRollsWasGiven");
        }
        return "\n";
    }

    private int getInvalidArgument(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (!diceRegEX.matcher(args[i]).matches()) {
                return i;
            }
        }
        return -1;
    }

    private class DiceRoll {

        private final String dice;

        private int sum = 0;
        private List<String> numbers = new ArrayList<>();

        DiceRoll(String dice) {
            this.dice = dice;
        }

        void addNumber(int number) {
            numbers.add("" + number);
            sum += number;
        }

        String getDice() {
            return dice.toUpperCase();
        }

        int getSum() {
            return sum;
        }

        List<String> getNumbers() {
            return numbers;
        }
    }
}
