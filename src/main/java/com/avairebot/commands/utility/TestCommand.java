package com.avairebot.commands.utility;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.cache.CacheType;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.administration.LevelCommand;
import com.avairebot.contracts.commands.Command;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.controllers.PlayerController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.database.transformers.PlayerTransformer;
import com.avairebot.imagegen.Fonts;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"Duplicates"})
public class TestCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(TestCommand.class);
    private final String cacheToken = "database-user-scores.";

    public TestCommand(AvaIre avaire) {
        super(avaire, false);
    }

    public static BufferedImage generateImage(
        final String avatarUrl,
        final String username,
        final String discriminator,
        final String rank,
        final String level,
        final String currentXpInLevel,
        final String missingXpToNextLevel,
        final String serverExperience,
        final String globalExperience,
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
        final Color experienceBackground = new Color(38F / 255F, (float) 39 / 255, (float) 59 / 255, 0.6F);
        final Color experienceForeground = new Color((float) 104 / 255, (float) 107 / 255, (float) 170 / 255, 0.8F);
        final Color experienceDelimiter = new Color((float) 140 / 255, (float) 144 / 255, (float) 226 / 255, 0.8F);
        final Color experienceText = new Color((float) 226 / 255, (float) 226 / 255, (float) 229 / 255, 0.85F);

        // Create our images
        BufferedImage avatarImage = resize(ImageIO.read(urlConnection.getInputStream()), 95, 95);
        BufferedImage backgroundImage = resize(ImageIO.read(TestCommand.class.getClassLoader().getResourceAsStream("backgrounds/test.jpg")), 200, 600);
//        BufferedImage backgroundImage = new BufferedImage(600, 200, BufferedImage.TYPE_INT_ARGB);

        // Merges the avatar with the background
        Graphics2D avatarGraphics = backgroundImage.createGraphics();
//        avatarGraphics.setColor(Color.decode("#32363C"));
//        avatarGraphics.fillRect(0, 0, 600, 200);
        avatarGraphics.drawImage(avatarImage, 25, 15, null);

        // Creates our text graphic, draws our text on top of the background
        Graphics2D textGraphics = backgroundImage.createGraphics();
        textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        textGraphics.setFont(Fonts.medium.deriveFont(Font.BOLD, 26F));
        textGraphics.drawString(username, startingX + 5, startingY);
        FontMetrics textGraphicsFontMetrics = textGraphics.getFontMetrics();
        textGraphics.setFont(Fonts.medium.deriveFont(Font.PLAIN, 17));
        textGraphics.setColor(Color.decode("#A6A6A6"));
        textGraphics.drawString("#" + discriminator, startingX + 5 + textGraphicsFontMetrics.stringWidth(username), startingY);

        // Creates a background bar for the XP
        Graphics2D experienceGraphics = backgroundImage.createGraphics();
        experienceGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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
        Font smallText = Fonts.regular.deriveFont(Font.BOLD, 20F);
        experienceGraphics.setFont(smallText);
        FontMetrics fontMetrics = experienceGraphics.getFontMetrics(smallText);
        experienceGraphics.drawString(xpBarText, startingX + 5 + ((xpBarLength - fontMetrics.stringWidth(xpBarText)) / 2), startingY + 42);

        // Create Level, Rank, and Total XP Text
        Graphics2D infoTextGraphics = backgroundImage.createGraphics();
        infoTextGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        infoTextGraphics.setColor(experienceText);
        // Create Level text
        infoTextGraphics.setFont(Fonts.medium.deriveFont(Font.PLAIN, 28));
        infoTextGraphics.drawString("LEVEL", 35, 140);
        FontMetrics infoTextGraphicsFontMetricsLarge = infoTextGraphics.getFontMetrics();
        infoTextGraphics.setFont(Fonts.bold.deriveFont(Font.BOLD, 48));
        FontMetrics infoTextGraphicsFontMetricsSmall = infoTextGraphics.getFontMetrics();
        infoTextGraphics.drawString(level, 35 + ((infoTextGraphicsFontMetricsLarge.stringWidth("LEVEL") - infoTextGraphicsFontMetricsSmall.stringWidth(level)) / 2), 185);

        // Create Score Text
        infoTextGraphics.setFont(Fonts.medium.deriveFont(Font.PLAIN, 28));
        infoTextGraphics.drawString("RANK", 165, 140);
        infoTextGraphics.setFont(Fonts.bold.deriveFont(Font.BOLD, 48));
        infoTextGraphics.drawString(rank, 165 + ((infoTextGraphicsFontMetricsLarge.stringWidth("RANK") - infoTextGraphicsFontMetricsSmall.stringWidth(rank)) / 2), 185);

        // Create XP states
        infoTextGraphics.setFont(Fonts.medium.deriveFont(Font.PLAIN, 26F));
        infoTextGraphics.drawString("Server XP:", 300, 140);
        infoTextGraphics.drawString("Global XP:", 300, 180);
        infoTextGraphics.setFont(Fonts.regular.deriveFont(Font.PLAIN, 24F));
        infoTextGraphics.setColor(Color.decode("#A6A6A6"));
        infoTextGraphics.drawString(serverExperience, 455, 140);
        infoTextGraphics.drawString(globalExperience, 455, 180);

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

        loadProperties(context, context.getAuthor()).thenAccept(properties -> {
            String score = properties.getScore().equals("Unranked")
                ? "Unranked"
                : properties.getScore();

            long experience = player.getExperience();
            long level = avaire.getLevelManager().getLevelFromExperience(guildTransformer, experience);
            long current = avaire.getLevelManager().getExperienceFromLevel(guildTransformer, level);

            long nextLevelXp = avaire.getLevelManager().getExperienceFromLevel(guildTransformer, level + 1);
            double percentage = ((double) (experience - current) / (nextLevelXp - current)) * 100;

            log.info("Percentage: " + percentage);

            try {
                BufferedImage bufferedImage = generateImage(
                    context.getAuthor().getEffectiveAvatarUrl(),
                    context.getAuthor().getName(),
                    context.getAuthor().getDiscriminator(),
                    score,
                    NumberUtil.formatNicely(level),
                    NumberUtil.formatNicely(experience - current),
                    NumberUtil.formatNicely(nextLevelXp - current),
                    NumberUtil.formatNicely(experience - 100),
                    NumberUtil.formatNicely(properties.getTotal()),
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
        });

        return true;
    }

    private CompletableFuture<DatabaseProperties> loadProperties(CommandMessage context, User author) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerTransformer player = context.getAuthor().getIdLong() == author.getIdLong()
                    ? context.getPlayerTransformer() : PlayerController.fetchPlayer(avaire, context.getMessage(), author);

                DataRow data = avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                    .selectRaw("sum(`experience`) - (count(`user_id`) * 100) as `total`")
                    .where("user_id", author.getId())
                    .get().first();

                long total = data == null ? (player == null ? 0 : player.getExperience()) : data.getLong("total");

                return new DatabaseProperties(player, total, getScore(context, author.getId()));
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }


    private String getScore(CommandMessage context, String userId) throws SQLException {
        if (avaire.getCache().getAdapter(CacheType.MEMORY).has(cacheToken + context.getGuild().getId())) {
            Collection users = (Collection) avaire.getCache().getAdapter(CacheType.MEMORY).get(cacheToken + context.getGuild().getId());
            String score = "???"; // Unranked score

            for (int i = 0; i < users.size(); i++) {
                if (Objects.equals(users.get(i).getString("id"), userId)) {
                    score = "" + (i + 1);
                    break;
                }
            }

            return score;
        }

        avaire.getCache().getAdapter(CacheType.MEMORY).put(cacheToken + context.getGuild().getId(),
            avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
                .select("user_id as id")
                .orderBy("experience", "desc")
                .where("guild_id", context.getGuild().getId())
                .get(),
            120
        );

        return getScore(context, userId);
    }

    private long getUsersInGuild(Guild guild) {
        return guild.getMembers().stream().filter(member -> !member.getUser().isBot()).count();
    }

    private class DatabaseProperties {

        private final PlayerTransformer player;
        private final long total;
        private final String score;

        DatabaseProperties(PlayerTransformer player, long total, String score) {
            this.player = player;
            this.total = total;
            this.score = score;
        }

        public PlayerTransformer getPlayer() {
            return player;
        }

        long getTotal() {
            return total;
        }

        String getScore() {
            return score;
        }
    }
}
