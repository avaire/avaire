package com.avairebot.commands.utility;

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


import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.utilities.ColorUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ColorCommand extends Command {

    public ColorCommand(AvaIre avaire) {
        super(avaire);
    }


    @Override
    public String getName() {
        return "Color Command";
    }


    @Override
    public List<String> getTriggers() {
        return Arrays.asList("color", "clr");
    }

    @Override
    public String getDescription() {
        return "Shows you what color corresponds to the provided hex.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <hex>` - Shows what the provided hex color looks like");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command #00ff00`"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0)
        {
            return sendErrorMessage(context, "Missing argument `hex`, you must include a hex color");
        }

        Color color = ColorUtil.getColorFromString(args[0]);
        if (color == null)
        {
            context.makeInfo(context.i18n("errorParsingColor"))
                .set("color", args[0]).queue();
            return true;
        }

        String hex = args[0];
        BufferedImage bufferedImage = createRectangle(color);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            ImageIO.write(bufferedImage, "jpg", baos);
        } catch (IOException e)
        {
            return sendErrorMessage(context, "somethingWentWrong");
        }
        byte[] bytes = baos.toByteArray();

        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = context.makeEmbeddedMessage()
            .setImage("attachment://" + getClass().getSimpleName() + "-" + hex + ".png")
            .setDescription(hex)
            .requestedBy(context)
            .build();
        messageBuilder.setEmbed(embedBuilder.build());

        context.getMessageChannel().sendFile(bytes, getClass().getSimpleName() + "-" + hex + ".png").queue();
        return true;
    }


    private BufferedImage createRectangle(Color color) {
        BufferedImage bufferedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = bufferedImage.createGraphics();

        graphics.setPaint(color);

        graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());


        return bufferedImage;

    }
}
