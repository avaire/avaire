/*
 * Copyright (c) 2019.
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
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.utilities.ImageUtil;
import com.avairebot.utilities.NumberUtil;
import com.avairebot.utilities.RandomUtil;
import com.avairebot.utilities.ResourceLoaderUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlotCommand extends Command {
    private int defaultColumnCount = 3;

    private int defaultRowCount = 3;

    private String[] emotes = new String[]{"🍎", "🍊", "🍐", "🍋", "🍉", "🍇", "🍓", "🍒"};

    private int columnCount = 3;

    private int rowCount = 3;

    public SlotCommand(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getDescription() {
        return "Rolls the slots.";
    }

    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName() {
        return "Slot Command";
    }

    /**
     * Gets am immutable list of command triggers that can be used to invoke the current
     * command, the first index in the list will be used when the `:command` placeholder
     * is used in {@link #getDescription(CommandContext)} or {@link #getUsageInstructions()} methods.
     *
     * @return An immutable list of command triggers that should invoked the command.
     */
    @Override
    public List<String> getTriggers() {
        return Arrays.asList("slots", "bet");
    }

    public boolean hasGraphics() throws IOException {
        return !ResourceLoaderUtil.getFiles(SlotCommand.class, "/command_pictures/slots", true).isEmpty();
    }

    /**
     * The command executor, this method is invoked by the command handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its command triggers.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    @Override
    public boolean onCommand(CommandMessage context, String[] args)
    {
        try
        {
            if (args.length > 0 && args[0].equals("graphical") && hasGraphics())
            {
                if (!context.getChannel().isNSFW() && context.isGuildMessage())
                {
                    return sendErrorMessage(context, context.i18n("nsfwDisabled"));
                }
                sendPictureBased(context, args);
            }
            else
            {
                sendEmoteBased(context, args);
            }
        } catch (IOException ex)
        {
            sendEmoteBased(context, args);
        }
        return true;
    }

    private void sendPictureBased(CommandMessage context, String[] args) throws IOException {
        int betAmount = 0;
        if (args.length == 2 && NumberUtil.isNumeric(args[1]))
        {
            betAmount = NumberUtil.getBetween(NumberUtil.parseInt(args[1]), 0, Integer.MAX_VALUE);
        }

        List<String> slots = new ArrayList<>();
        BufferedImage background = ImageIO.read(SlotCommand.class.getClassLoader().getResourceAsStream("command_pictures/slots/bg.png"));
        List<String> emojiFiles = ResourceLoaderUtil.getFiles(SlotCommand.class, "command_pictures/slots/emoji");


        for (int column = 0; column < defaultColumnCount; column++)
        {
            String emote = (String) RandomUtil.pickRandom(emojiFiles);
            slots.add(emote);
            BufferedImage emoteImage = ImageIO.read(SlotCommand.class.getClassLoader().getResourceAsStream("command_pictures/slots/emoji/" + emote));
            ImageUtil.drawOnTopOfOtherImage(background, emoteImage, 100, 95 + 142 * column, 330);
        }

        int printWon = (int) calculateReturn(slots, betAmount);
        int n = 0;

        do
        {
            int digit = printWon % 10;
            printWon = printWon / 10;
            BufferedImage image = ImageIO.read(SlotCommand.class.getClassLoader().getResourceAsStream("command_pictures/slots/numbers/" + digit + ".png"));
            ImageUtil.drawOnTopOfOtherImage(background, image, 100, 230 - n * 16, 462);
            n++;
        } while ((printWon / 10) != 0);

        n = 0;
        int printBet = betAmount;

        do
        {
            int digit = (printBet % 10);
            printBet = printBet / 10;
            BufferedImage image = ImageIO.read(SlotCommand.class.getClassLoader().getResourceAsStream("command_pictures/slots/numbers/" + digit + ".png"));
            ImageUtil.drawOnTopOfOtherImage(background, image, 100, 395 - n * 16, 462);
            n++;
        }while ((printBet / 10) != 0);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();


        ImageIO.write(background,"png",baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        if(isCompleteMatch(slots.subList(0,3),defaultColumnCount))
        {
            sendGraphicalWinStateMessage(context,imageInByte,"won");
        }

        else if(isPartialMatch((slots.subList(0,3)),defaultColumnCount))
        {
            sendGraphicalWinStateMessage(context,imageInByte,"almostWon");
        }

        else if(!isPartialMatch(slots.subList(0,3),defaultColumnCount) )
        {
            sendGraphicalWinStateMessage(context,imageInByte,"lost");
        }
    }

    private void sendEmoteBased(CommandMessage context, String[] args)
    {
        int columnCount = defaultColumnCount;
        if(args.length == 1 && NumberUtil.isNumeric(args[0]))
        {
            columnCount = NumberUtil.getBetween(NumberUtil.parseInt(args[0]),0,Integer.MAX_VALUE);
        }

        List<String> slots = new ArrayList<>();

        StringBuilder rolled = new StringBuilder();

        for(int column = 0; column < columnCount; column++)
        {
            rolled.append("[");
            for(int row = 0; row < defaultRowCount; row++)
            {
                String emote = RandomUtil.pickRandom(emotes);
                slots.add(emote);
                rolled.append(emote);
            }
            rolled.append("]");
            rolled.append("\n");
        }

        if(isCompleteMatch(slots,columnCount) )
        {
            context.makeSuccess(context.i18n("rolled").replace(":user",context.getAuthor().getName()
            )  + rolled.toString() + "\n" + context.i18n("won")).queue();
        }

        else if(isPartialMatch(slots,columnCount) )
        {
            context.makeSuccess(context.i18n("rolled").replace(":user",context.getAuthor().getName()
            )  + rolled.toString() + "\n" + context.i18n("almostWon")).queue();
        }

        else if(!isPartialMatch(slots,columnCount))
        {
            context.makeSuccess(context.i18n("rolled").replace(":user",context.getAuthor().getName()
            )  + rolled.toString() + "\n" + context.i18n("lost")).queue();
        }
    }


    private float calculateReturn(List<String> slots, int bet)
    {
        if(isCompleteMatch(slots.subList(0,3),defaultColumnCount))
        {
            return bet * 2f;
        }
        else if(isPartialMatch(slots.subList(0,3),defaultColumnCount))
        {
            return bet;
        }
        else
        {
            return 0;
        }
    }


    private void sendGraphicalWinStateMessage(CommandMessage context, byte[] imageInByte, String winState)
    {
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = context.makeEmbeddedMessage()
            .setImage("attachment://" + getClass().getSimpleName() + "-slots.png")
            .setColor(Color.decode("#3a71c1"))
            .setDescription(buildMessage(context,winState))
            .requestedBy(context)
            .build();
        messageBuilder.setEmbed(embedBuilder.build());
        context.getMessageChannel().sendFile(imageInByte, getClass().getSimpleName() + "-slots.png", messageBuilder.build()).queue();
    }



    private String buildMessage(CommandMessage context, String winState) {
        return context.i18n("rolled").replace(":user",context.getAuthor().getName()
        )  + context.i18n(winState);
    }

    private boolean isCompleteMatch(List<String> emotes, int columnCount)
    {
        int length = emotes.size();
        length /= columnCount;
        for(int i = 1; length > 0 ; i++)
        {
            length /= columnCount;
            if ( IsRowCompleteMatch(emotes.subList(columnCount* ( i - 1),columnCount * i)))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isPartialMatch(List<String> emotes, int columnCount)
    {
        int length = emotes.size() ;
        length /= columnCount;
        for(int i = 1; length > 0; i++)
        {
            length /= columnCount;
            if ( isRowPartialMatch(emotes.subList(columnCount * (i - 1),columnCount * i)))
            {
                return true;
            }
        }
        return false;
    }


    private boolean IsRowCompleteMatch(List<String> emotes)
    {

        for(int i = 0; i < emotes.size(); i++)
        {
            for(int w = 0; w < emotes.size() - i; w++)
            {
                if(!emotes.get(i).equals(emotes.get(w)))
                {
                    return false;
                }
            }
        }
        return true;
    }


    private boolean isRowPartialMatch(List<String> emotes)
    {
        for(int i = 0; i < emotes.size(); i++)
        {
            for(int w = i + 1; w < emotes.size() - i; w++)
            {
                if(emotes.get(i).equals(emotes.get(w)))
                {
                    return true;
                }
            }
        }
        return false;
    }


}
