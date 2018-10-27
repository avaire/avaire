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

package com.avairebot.contracts.imagegen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public abstract class BackgroundRankColors implements BackgroundColors {

    /**
     * The color that should be used for the background, this will only used if
     * the {@link com.avairebot.imagegen.RankBackgrounds rank background}
     * doesn't have a background resource.
     *
     * @return The background color that should be used for the image.
     */
    @Nonnull
    public Color getBackgroundColor() {
        return makeColor(50, 54, 60);
    }

    /**
     * The color that should be used for the background cover, if one is set the cover
     * will be placed behind all the other resources with a few pixels width between
     * the cover and the edge of the image itself, it works great if the text and
     * the image resource has a similar colour scheme.
     * <p>
     * If the background color is set to <code>NULL</code>
     * no cover will be used for the image.
     *
     * @return The background color that should be used for the image, or <code>NULL</code> if none should be used.
     */
    @Nullable
    public Color getBackgroundCoverColor() {
        return null;
    }

    /**
     * The color that should be used as the main text color, this includes
     * the users username, level, rank, server XP, and global XP text.
     *
     * @return The color that should be used as the main text color.
     */
    @Nonnull
    public abstract Color getMainTextColor();

    /**
     * The color that should be used as the secondary text color, this includes
     * the the users discriminator, and the server and global XP values.
     *
     * @return The color that should be used as the secondary text color.
     */
    @Nonnull
    public abstract Color getSecondaryTextColor();

    /**
     * The color that should be used as the experience bar background.
     *
     * @return The color that should be used as the experience bar background.
     */
    @Nonnull
    public abstract Color getExperienceBackgroundColor();

    /**
     * The color that should be used as the experience bar foreground.
     *
     * @return The color that should be used as the experience bar foreground.
     */
    @Nonnull
    public abstract Color getExperienceForegroundColor();

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
    public abstract Color getExperienceSeparatorColor();

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
        return getMainTextColor();
    }
}
