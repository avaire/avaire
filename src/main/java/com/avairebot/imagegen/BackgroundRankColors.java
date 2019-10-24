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

package com.avairebot.imagegen;

import com.avairebot.utilities.ColorUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * The background rank colors used for making
 * the {@link com.avairebot.imagegen.RankBackground}
 */
@SuppressWarnings("WeakerAccess")
public class BackgroundRankColors {

    private Color backgroundColor = ColorUtil.makeColor(55, 55, 70);
    private Color backgroundCoverColor = null;
    private Color mainTextColor = ColorUtil.makeColor(226, 226, 229, 85);
    private Color experienceTextColor = null;
    private Color secondaryTextColor = ColorUtil.makeColor(166, 166, 166, 85);
    private Color experienceBackgroundColor = ColorUtil.makeColor(38, 39, 59, 60);
    private Color experienceForegroundColor = ColorUtil.makeColor(104, 107, 170, 80);
    private Color experienceSeparatorColor = ColorUtil.makeColor(140, 144, 226, 80);

    /**
     * The color that should be used for the background, this will only used if
     * the {@link RankBackground rank background}
     * doesn't have a background resource.
     *
     * @return The background color that should be used for the image.
     */
    @Nonnull
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the color that should be used for the background cover, if one is set the cover
     * will be placed behind all the other resources with a few pixels width between
     * the cover and the edge of the image itself, it works great if the text and
     * the image resource has a similar colour scheme.
     * <p>
     */
    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    /**
     * The color that should be used for the background cover, if one is set, the cover
     * will be placed behind all the other resources with a few pixels width between
     * the cover and the edge of the image itself, works great if the text and
     * the image resource has a similar colour scheme.
     * <p>
     * If the background color is set to <code>NULL</code>
     * no cover will be used for the image.
     *
     * @return The background color that should be used for the image, or <code>NULL</code> if none should be used.
     */
    @Nullable
    public Color getBackgroundCoverColor() {
        return backgroundCoverColor;
    }

    /**
     * Sets background cover color.
     * <p>
     * <b>Note:</b> The cover color is a layer inside the rank background that covers most
     * of the background size, allowing some content to pop out more, this value is
     * generally set to an color with an alpha value to create some transparency.
     *
     * @param color The new background cover color.
     */
    public void setBackgroundCoverColor(Color color) {
        backgroundCoverColor = color;
    }

    /**
     * The color that should be used as the main text color, includes
     * the users username, level, rank, server XP, and global XP text.
     *
     * @return The color that should be used as the main text color.
     */
    @Nonnull
    public Color getMainTextColor() {
        return mainTextColor;
    }

    /**
     * Sets the main text color, used for the username,
     * level, rank, server XP, and global XP text.
     *
     * @param color The new main color text color.
     */
    public void setMainTextColor(Color color) {
        mainTextColor = color;
    }

    /**
     * The color that should be used as the secondary text color, this includes
     * the users discriminator and the server XP value as well as global XP values.
     *
     * @return The color that should be used as the secondary text color.
     */
    @Nonnull
    public Color getSecondaryTextColor() {
        return secondaryTextColor;
    }

    /**
     * Sets the secondary text color, this will be used for a users
     * discriminator, and the server XP value as well as global XP values.
     *
     * @param color The new secondary color text color.
     */
    public void setSecondaryTextColor(Color color) {
        secondaryTextColor = color;
    }

    /**
     * The color that should be used as the experience bar background.
     *
     * @return The color that should be used as the experience bar background.
     */
    @Nonnull
    public Color getExperienceBackgroundColor() {
        return experienceBackgroundColor;
    }

    /**
     * Sets experience background color.
     *
     * @param color The new experience background color.
     */
    public void setExperienceBackgroundColor(Color color) {
        experienceBackgroundColor = color;
    }

    /**
     * The sRGB color that should be used as the experience bar foreground.
     *
     * @return The color that should be used as the experience bar foreground.
     */
    @Nonnull
    public Color getExperienceForegroundColor() {
        return experienceForegroundColor;
    }

    /**
     * Sets the experience foreground color using a valid sRGB color.
     *
     * @param color The new experience foreground color. .
     */
    public void setExperienceForegroundColor(Color color) {
        experienceForegroundColor = color;
    }

    /**
     * The color that should be used as the experience bar separator,
     * the separator is a five pixel width bar at the end of the
     * "current xp bar", creating a clear separation between
     * the amount of XP the user has, vs the remaining XP
     * before the user levels up again..
     *
     * @return The color that should be used as the experience bar background.
     */
    @Nonnull
    public Color getExperienceSeparatorColor() {
        return experienceSeparatorColor;
    }

    /**
     * Sets experience separator color, the separator is a five pixel width
     * bar at the end of the "current xp bar", creating a clear separation
     * between the amount of XP the user has, vs the remaining XP
     * before the user levels up again.
     *
     * @param color The new experience separator color.
     */
    public void setExperienceSeparatorColor(Color color) {
        experienceSeparatorColor = color;
    }

    /**
     * The color that should be used on the experience bar, by default this will
     * be the same color as the {@link #getMainTextColor() main text color},
     * however in some cases the main color and the XP bar background
     * might be too similar in color, so this can be used to
     * overwrite the XP bar text color.
     *
     * @return The color that should be used for the experience bar text.
     */
    @Nonnull
    public Color getExperienceTextColor() {
        if (experienceTextColor == null) {
            return getMainTextColor();
        }
        return experienceTextColor;
    }

    /**
     * Sets the sRGB color that should be used on the experience bar, by default this will
     * be the same color as the {@link #getMainTextColor() main text color},
     * however in some cases the main color and the XP bar background
     * might be too similar in color, so this can be used to
     * overwrite the XP bar text color.
     *
     * @param color: The color that should be used for the experience bar text.
     */
    public void setExperienceTextColor(Color color) {
        experienceTextColor = color;
    }
}
