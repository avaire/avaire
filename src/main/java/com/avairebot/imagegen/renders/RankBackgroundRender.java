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

package com.avairebot.imagegen.renders;

import com.avairebot.contracts.imagegen.Renderer;
import com.avairebot.imagegen.Fonts;
import com.avairebot.imagegen.RankBackgrounds;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

@SuppressWarnings("FieldCanBeLocal")
public class RankBackgroundRender extends Renderer {

    private final int xpBarLength = 420;
    private final int startingX = 145;
    private final int startingY = 35;

    private final String username;
    private final String discriminator;
    private final String avatarUrl;

    @Nonnull
    private RankBackgrounds background;

    private String rank = null;
    private String level = null;
    private String currentXpInLevel = null;
    private String totalXpInLevel = null;
    private String serverExperience = null;
    private String globalExperience = null;
    private double percentage = -1;

    /**
     * Creates the rank background render instance with the
     * given users username, discriminator, and avatar.
     *
     * @param user The user that should be sued for the rank background render.
     */
    public RankBackgroundRender(@Nonnull User user) {
        this(user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl());
    }

    /**
     * Creates the rank background render instance with the given username, discriminator,
     * and avatar URL, the avatar URL must be a link to a valid image.
     *
     * @param username      The username that should be used.
     * @param discriminator The discriminator that should be used.
     * @param avatarUrl     The avatar URL that should be used.
     */
    public RankBackgroundRender(String username, String discriminator, String avatarUrl) {
        this.username = username;
        this.discriminator = discriminator;
        this.avatarUrl = avatarUrl;
        this.background = RankBackgrounds.getDefaultBackground();
    }

    /**
     * Sets the background that should be used for the render, if <code>NULL</code> is given the
     * {@link RankBackgrounds#DEFAULT_BACKGROUND default background} will be used instead.
     *
     * @param background The background that should be used.
     * @return The rank background instance.
     */
    public RankBackgroundRender setBackground(@Nullable RankBackgrounds background) {
        this.background = background == null
            ? RankBackgrounds.getDefaultBackground()
            : background;

        return this;
    }

    /**
     * Sets the rank that should be displayed on the finished image.
     *
     * @param rank The users rank that should be displayed on the finished image.
     * @return The rank background instance.
     */
    public RankBackgroundRender setRank(@Nonnull String rank) {
        this.rank = rank;
        return this;
    }

    /**
     * Sets the level that should be displayed on the finished image.
     *
     * @param level The users level that should be displayed on the finished image.
     * @return The rank background instance.
     */
    public RankBackgroundRender setLevel(@Nonnull String level) {
        this.level = level;
        return this;
    }

    /**
     * Sets the current amount of XP the user has into their current level.
     *
     * @param currentXpInLevel The amount of XP the user has into their current level.
     * @return The rank background instance.
     */
    public RankBackgroundRender setCurrentXpInLevel(@Nonnull String currentXpInLevel) {
        this.currentXpInLevel = currentXpInLevel;
        return this;
    }

    /**
     * Sets the total amount of XP needed for the users current level.
     *
     * @param totalXpInLevel The total amount of XP needed for the users current level.
     * @return The rank background instance.
     */
    public RankBackgroundRender setTotalXpInLevel(@Nonnull String totalXpInLevel) {
        this.totalXpInLevel = totalXpInLevel;
        return this;
    }

    /**
     * Sets the users total amount of XP they have on the current server.
     *
     * @param serverExperience The users total amount of XP they have on the current server.
     * @return The rank background instance.
     */
    public RankBackgroundRender setServerExperience(@Nonnull String serverExperience) {
        this.serverExperience = serverExperience;
        return this;
    }

    /**
     * Sets the users total amount of XP they have globally.
     *
     * @param globalExperience The users total amount of XP they have globally.
     * @return The rank background instance.
     */
    public RankBackgroundRender setGlobalExperience(@Nonnull String globalExperience) {
        this.globalExperience = globalExperience;
        return this;
    }

    /**
     * Sets the percentage of progress the user has made into their current level.
     *
     * @param percentage The progress the user has made into their current level.
     * @return The rank background instance.
     */
    public RankBackgroundRender setPercentage(double percentage) {
        this.percentage = percentage;
        return this;
    }

    @Override
    public boolean canRender() {
        return rank != null
            && level != null
            && currentXpInLevel != null
            && totalXpInLevel != null
            && serverExperience != null
            && globalExperience != null
            && percentage > -1;
    }

    @Override
    protected BufferedImage handleRender() throws IOException {
        URL url = new URL(avatarUrl);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "AvaIre-Discord-Bot");

        final String xpBarText = String.format("%s out of %s xp", currentXpInLevel, totalXpInLevel);

        BufferedImage backgroundImage = loadAndBuildBackground();

        // Creates our graphics and prepares it for use.
        Graphics2D graphics = backgroundImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (background.getBackgroundColors().getBackgroundCoverColor() != null) {
            graphics.setColor(background.getBackgroundColors().getBackgroundCoverColor());
            graphics.fillRect(17, 8, 566, 184);
        }

        // Draws the avatar image on top of the background.
        graphics.drawImage(resize(ImageIO.read(urlConnection.getInputStream()), 95, 95), 25, 15, null);

        createUserGraphics(graphics);
        createBackgroundGraphics(graphics, xpBarText);
        createLevelAndRankGraphics(graphics);
        createExperienceGraphics(graphics);

        return backgroundImage;
    }

    private BufferedImage loadAndBuildBackground() throws IOException {
        if (background.getBackgroundFile() != null) {
            return resize(
                ImageIO.read(Renderer.class.getClassLoader().getResourceAsStream("backgrounds/" + background.getBackgroundFile())),
                200, 600
            );
        }

        BufferedImage backgroundImage = new BufferedImage(600, 200, BufferedImage.TYPE_INT_ARGB);

        Graphics2D avatarGraphics = backgroundImage.createGraphics();
        avatarGraphics.setColor(background.getBackgroundColors().getBackgroundColor());
        avatarGraphics.fillRect(0, 0, 600, 200);

        return backgroundImage;
    }

    private void createUserGraphics(Graphics2D graphics) {
        graphics.setFont(Fonts.bold.deriveFont(Font.PLAIN, 26F));
        graphics.setColor(background.getBackgroundColors().getMainTextColor());

        graphics.drawString(username, startingX + 5, startingY);

        FontMetrics fontMetrics = graphics.getFontMetrics();

        graphics.setFont(Fonts.medium.deriveFont(Font.PLAIN, 17));
        graphics.setColor(background.getBackgroundColors().getSecondaryTextColor());

        graphics.drawString("#" + discriminator, startingX + 5 + fontMetrics.stringWidth(username), startingY);
    }

    private void createBackgroundGraphics(Graphics2D graphics, String xpBarText) {
        graphics.setColor(background.getBackgroundColors().getExperienceBackgroundColor());
        graphics.fillRect(startingX, startingY + 10, xpBarLength, 50);

        // Create the current XP bar for the background
        graphics.setColor(background.getBackgroundColors().getExperienceForegroundColor());
        graphics.fillRect(startingX + 5, startingY + 15, (int) Math.min(xpBarLength - 10, (xpBarLength - 10) * (percentage / 100)), 40);

        // Create a 5 pixel width bar that's just at the end of our "current xp bar"
        graphics.setColor(background.getBackgroundColors().getExperienceSeparatorColor());
        graphics.fillRect(startingX + 5 + (int) Math.min(xpBarLength - 10, (xpBarLength - 10) * (percentage / 100)), startingY + 15, 5, 40);

        // Create the text that should be displayed in the middle of the XP bar
        graphics.setColor(background.getBackgroundColors().getExperienceTextColor());

        Font smallText = Fonts.medium.deriveFont(Font.PLAIN, 20F);
        graphics.setFont(smallText);

        FontMetrics fontMetrics = graphics.getFontMetrics(smallText);
        graphics.drawString(xpBarText, startingX + 5 + ((xpBarLength - fontMetrics.stringWidth(xpBarText)) / 2), startingY + 42);
    }

    private void createLevelAndRankGraphics(Graphics2D graphics) {
        graphics.setColor(background.getBackgroundColors().getMainTextColor());

        // Create Level text
        graphics.setFont(Fonts.medium.deriveFont(Font.PLAIN, 28));
        graphics.drawString("LEVEL", 35, 140);

        FontMetrics infoTextGraphicsFontMetricsLarge = graphics.getFontMetrics();
        graphics.setFont(Fonts.extraBold.deriveFont(Font.PLAIN, 48));

        FontMetrics infoTextGraphicsFontMetricsSmall = graphics.getFontMetrics();
        graphics.drawString(level, 35 + (
            (infoTextGraphicsFontMetricsLarge.stringWidth("LEVEL") - infoTextGraphicsFontMetricsSmall.stringWidth(level)) / 2
        ), 185);

        // Create Score Text
        graphics.setFont(Fonts.medium.deriveFont(Font.PLAIN, 28));
        graphics.drawString("RANK", 165, 140);
        graphics.setFont(Fonts.extraBold.deriveFont(Font.PLAIN, 48));
        graphics.drawString(rank, 165 + (
            (infoTextGraphicsFontMetricsLarge.stringWidth("RANK") - infoTextGraphicsFontMetricsSmall.stringWidth(rank)) / 2
        ), 185);
    }

    private void createExperienceGraphics(Graphics2D graphics) {
        graphics.setColor(background.getBackgroundColors().getMainTextColor());

        graphics.setFont(Fonts.medium.deriveFont(Font.PLAIN, 26F));
        graphics.drawString("Server XP:", 300, 140);
        graphics.drawString("Global XP:", 300, 180);

        graphics.setFont(Fonts.regular.deriveFont(Font.PLAIN, 24F));
        graphics.setColor(background.getBackgroundColors().getSecondaryTextColor());
        graphics.drawString(serverExperience, 455, 140);
        graphics.drawString(globalExperience, 455, 180);
    }
}
