package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.administration.LevelCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.utilities.LevelUtil;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.MessageBuilder;
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

    public static BufferedImage generateImage(
        final String avatarUrl,
        final String username,
        final String discriminator,
        final String currentXpInLevel,
        final String missingXpToNextLevel,
        final double percentage
    ) throws IOException, FontFormatException {
        final long start = System.currentTimeMillis();

        URL url = new URL(avatarUrl);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "AvaIre-Discord-Bot");

        final int xpBarLength = 420;
        final int startingX = 145;
        final int startingY = 35;
        final String xpBarText = String.format("%s out of %s xp", currentXpInLevel, missingXpToNextLevel);

        // Colors
        final Color experienceBackground = new Color((float) 38 / 255, (float) 39 / 255, (float) 59 / 255, 0.6F);
        final Color experienceForeground = new Color((float) 104 / 255, (float) 107 / 255, (float) 170 / 255, 0.8F);
        final Color experienceDelimiter = new Color((float) 140 / 255, (float) 144 / 255, (float) 226 / 255, 0.8F);
        final Color experienceText = new Color((float) 226 / 255, (float) 226 / 255, (float) 229 / 255, 0.85F);

        // Create our images
        BufferedImage avatarImage = resize(ImageIO.read(urlConnection.getInputStream()), 95, 95);
//        BufferedImage backgroundImage = resize(ImageIO.read(TestCommand.class.getClassLoader().getResourceAsStream("backgrounds/example.jpg")), 200, 600);
        BufferedImage backgroundImage = new BufferedImage(600, 200, BufferedImage.TYPE_INT_ARGB);

        // Merges the avatar with the background
        Graphics2D avatarGraphics = backgroundImage.createGraphics();
        avatarGraphics.drawImage(avatarImage, 25, 15, null);

        // Creates our custom fonts
        Font mediumFont = Font.createFont(Font.TRUETYPE_FONT, TestCommand.class.getClassLoader().getResourceAsStream("fonts/Poppins-Medium.ttf"));
        Font boldFont = Font.createFont(Font.TRUETYPE_FONT, TestCommand.class.getClassLoader().getResourceAsStream("fonts/Poppins-Bold.ttf"));
        Font regularFont = Font.createFont(Font.TRUETYPE_FONT, TestCommand.class.getClassLoader().getResourceAsStream("fonts/Poppins-Regular.ttf"));

        // Creates our text graphic, draws our text on top of the background
        Graphics2D textGraphics = backgroundImage.createGraphics();
        textGraphics.setFont(mediumFont.deriveFont(Font.BOLD, 26F));
        textGraphics.drawString(username, startingX, startingY);
        FontMetrics textGraphicsFontMetrics = textGraphics.getFontMetrics();
        textGraphics.setFont(mediumFont.deriveFont(Font.PLAIN, 19));
        textGraphics.setColor(Color.decode("#C1C1C1"));
        textGraphics.drawString("#" + discriminator, startingX + textGraphicsFontMetrics.stringWidth(username), startingY);

        // Creates a background bar for the XP
        Graphics2D experienceGraphics = backgroundImage.createGraphics();
        experienceGraphics.setColor(experienceBackground);
        experienceGraphics.fillRect(startingX, startingY + 10, xpBarLength, 50);
        // Create the current XP bar for the background
        experienceGraphics.setColor(experienceForeground);
        experienceGraphics.fillRect(startingX + 5, startingY + 15, (int) Math.min(xpBarLength - 10, (xpBarLength - 10) * (percentage / 100)), 40);
        // Create a 5 pixel width bar that's just at the end of our "current xp bar"
        experienceGraphics.setColor(experienceDelimiter);
        experienceGraphics.fillRect(startingX + 5 + (int) Math.min(xpBarLength - 10, (xpBarLength - 10) * (percentage / 100)), startingY + 15, 5, 40);
        // Create the text that should be displayed in the middle of the XP bar
        experienceGraphics.setColor(experienceText);
        Font smallText = regularFont.deriveFont(Font.BOLD, 20F);
        experienceGraphics.setFont(smallText);
        FontMetrics fontMetrics = experienceGraphics.getFontMetrics(smallText);
        experienceGraphics.drawString(xpBarText, startingX + 5 + ((xpBarLength - fontMetrics.stringWidth(xpBarText)) / 2), startingY + 42);

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
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            return sendErrorMessage(
                context,
                "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            );
        }

        PlayerTransformer player = context.getDatabaseEventHolder().getPlayer();
        if (player == null) {
            return sendErrorMessage(context, "Player object was null, exiting");
        }

        long experience = player.getExperience();
        long level = LevelUtil.getLevelFromExperience(experience);
        long current = LevelUtil.getExperienceFromLevel(level);

        long nextLevelXp = LevelUtil.getExperienceFromLevel(level + 1);
        double percentage = ((double) (experience - current) / (nextLevelXp - current)) * 100;

        log.info("Percentage: " + percentage);

        try {
            BufferedImage bufferedImage = generateImage(
                context.getAuthor().getEffectiveAvatarUrl(),
                context.getAuthor().getName(),
                context.getAuthor().getDiscriminator(),
                NumberUtil.formatNicely(experience - current),
                NumberUtil.formatNicely(nextLevelXp - current),
                percentage
            );

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteStream);
            byteStream.flush();
            byte[] imageBytes = byteStream.toByteArray();
            byteStream.close();

            MessageBuilder message = new MessageBuilder();
            message.setEmbed(context.makeEmbeddedMessage()
                .setColor(Color.decode("#5C5F93"))
                .setImage("attachment://" + context.getAuthor().getId() + "-avatar.png")
                .buildEmbed()
            );
            context.getChannel().sendFile(imageBytes, context.getAuthor().getId() + "-avatar.png", message.build()).queue();
        } catch (IOException | FontFormatException e) {
            context.makeError("Failed to run test command: " + e.getMessage()).queue();
            e.printStackTrace();
        }

        return true;
    }
}
