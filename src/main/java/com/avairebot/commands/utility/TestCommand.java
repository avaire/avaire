package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

public class TestCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(TestCommand.class);

    public TestCommand(AvaIre avaire) {
        super(avaire, false);
    }

    public static BufferedImage generateImage(String avatarUrl) throws IOException, FontFormatException {
        final long start = System.currentTimeMillis();

        URL url = new URL(avatarUrl);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "AvaIre-Discord-Bot");

        final int xpBarLength = 375;
        final String xpBarText = "37,946 out of 104,462 xp";

        // Create our images
        BufferedImage avatarImage = resize(ImageIO.read(urlConnection.getInputStream()), 150, 150);
        BufferedImage backgroundImage = resize(ImageIO.read(TestCommand.class.getClassLoader().getResourceAsStream("backgrounds/example.jpg")), 200, 600);

        // Merges the avatar with the background
        Graphics2D avatarGraphics = backgroundImage.createGraphics();
        avatarGraphics.drawImage(avatarImage, 25, 25, null);

        // Creates our text graphic
        Graphics2D textGraphics = backgroundImage.createGraphics();

        // Creates our custom font, sets a type and size, and draws our test on top of the background
        Font font = Font.createFont(Font.TRUETYPE_FONT, TestCommand.class.getClassLoader().getResourceAsStream("fonts/FiraCode-Medium.ttf"));
        textGraphics.setFont(font.deriveFont(Font.BOLD, 20F));
        textGraphics.drawString("Senither#0001", 190, 60);

        // Creates a background bar for the XP
        Graphics2D experienceGraphics = backgroundImage.createGraphics();
        experienceGraphics.setColor(new Color((float) 38 / 255, (float) 39 / 255, (float) 59 / 255, 0.6F));
        experienceGraphics.fillRect(190, 80, xpBarLength, 40);
        // Create the current XP bar for the background
        experienceGraphics.setColor(new Color((float) 88 / 255, (float) 88 / 255, (float) 132 / 255, 0.8F));
        experienceGraphics.fillRect(195, 85, Math.min(xpBarLength - 10, 654984), 30);
        // Create the text that should be displayed in the middle of the XP bar
        experienceGraphics.setColor(new Color((float) 226 / 255, (float) 226 / 255, (float) 229 / 255, 0.85F));
        Font smallText = font.deriveFont(Font.BOLD, 14F);
        experienceGraphics.setFont(smallText);
        FontMetrics fontMetrics = experienceGraphics.getFontMetrics(smallText);
        experienceGraphics.drawString(xpBarText, 195 + ((xpBarLength - fontMetrics.stringWidth(xpBarText)) / 2), 105);

        log.info("Finished in {} ms", System.currentTimeMillis() - start);

        return backgroundImage;
    }

    private static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    @Override
    public String getName() {
        return "Test Command";
    }

    @Override
    public String getDescription() {
        return "Runs the test command to do a thing.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("test");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        try {
            BufferedImage bufferedImage = generateImage(context.getAuthor().getEffectiveAvatarUrl());

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteStream);
            byteStream.flush();
            byte[] imageBytes = byteStream.toByteArray();
            byteStream.close();

            context.getChannel().sendFile(imageBytes, context.getAuthor().getId() + "-avatar.png").queue();
        } catch (IOException | FontFormatException e) {
            context.makeError("Failed to run test command: " + e.getMessage()).queue();
            e.printStackTrace();
        }

        return true;
    }
}
