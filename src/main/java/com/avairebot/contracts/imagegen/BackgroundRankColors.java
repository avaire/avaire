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

import com.avairebot.imagegen.RankBackground;
import com.avairebot.utilities.ColorUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * The background rank colors used for making
 * the {@link com.avairebot.imagegen.RankBackground}
 */
public class BackgroundRankColors{

    private Color backgroundColor = makeColor(55, 55, 70);;

    private Color backgroundCoverColor = null;

    private Color mainTextColor = makeColor(226, 226, 229, 85);;

    private Color secondaryTextColor = makeColor(166, 166, 166, 85);

    private Color experienceBackgroundColor = makeColor(38, 39, 59, 60);

    private Color experienceForegroundColor = makeColor(104, 107, 170, 80);

    private Color experienceSeparatorColor =  makeColor(140, 144, 226, 80);

    public BackgroundRankColors()
    {

    }

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
     * Sets background color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     */
    public void setBackgroundColor(float red, float green, float blue)
    {
        backgroundColor = makeColor(red, green, blue);
    }

    /**
     * Sets background color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     * @param alpha the alpha
     */
    public void setBackgroundColor(float red, float green, float blue, float alpha)
    {
        backgroundColor = makeColor(red, green, blue,alpha);
    }

    /**
     * Sets background color.
     *
     * @param color the new color
     */
    public void setBackgroundColor(Color color)
    {
        backgroundColor = color;
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
        return backgroundCoverColor;
    }


    /**
     * Sets background cover color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     */
    public void setBackgroundCoverColor(float red, float green, float blue)
    {
        backgroundCoverColor = makeColor(red, green, blue);
    }

    /**
     * Sets background color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     * @param alpha the alpha
     */
    public void setBackgroundCoverColor(float red, float green, float blue, float alpha)
    {
        backgroundCoverColor = makeColor(red, green, blue, alpha);
    }

    /**
     * The color that should be used as the main text color, this includes
     * the users username, level, rank, server XP, and global XP text.
     *
     * @return The color that should be used as the main text color.
     */
    @Nonnull
    public Color getMainTextColor()
    {
        return mainTextColor;
    }

    /**
     * Sets main text color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     */
    public void setMainTextColor(float red, float green, float blue)
    {
        mainTextColor = makeColor(red,green,blue);
    }

    /**
     * Sets main text color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     * @param blue  the alpha
     */
    public void setMainTextColor(float red, float green, float blue, float alpha)
    {
        mainTextColor = makeColor(red,green,blue, alpha);
    }

    /**
     * Sets main text color.
     *
     * @param color the new color
     */
    public void setMainTextColor(Color color)
    {
        mainTextColor = color;
    }


    /**
     * The color that should be used as the secondary text color, this includes
     * the the users discriminator, and the server and global XP values.
     *
     * @return The color that should be used as the secondary text color.
     */
    @Nonnull
    public Color getSecondaryTextColor()
    {
        return secondaryTextColor;
    }

    /**
     * Sets secondary text color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     */
    public void setSecondaryTextColor(float red, float green, float blue)
    {
        secondaryTextColor = makeColor(red,green,blue);
    }

    /**
     * Sets main text color.
     *
     * @param color the new color
     */
    public void setSecondaryTextColor(Color color)
    {
        secondaryTextColor = color;
    }

    /**
     * Sets secondary text color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     * @param blue  the alpha
     */
    public void setSecondaryTextColor(float red, float green, float blue, float alpha)
    {
        secondaryTextColor = makeColor(red,green,blue, alpha);
    }

    /**
     * The color that should be used as the experience bar background.
     *
     * @return The color that should be used as the experience bar background.
     */
    @Nonnull
    public  Color getExperienceBackgroundColor()
    {
        return experienceBackgroundColor;
    }

    /**
     * Sets experience background color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     */
    public void setExperienceBackgroundColor(float red, float green, float blue)
    {
        experienceBackgroundColor = makeColor(red,green,blue);
    }

    /**
     * Sets experience background color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     * @param blue  the alpha
     */
    public void setExperienceBackgroundColor(float red, float green, float blue, float alpha)
    {
        experienceBackgroundColor = makeColor(red,green,blue, alpha);
    }

    /**
     * Sets experience background color.
     *
     * @param color the new color
     */
    public void setExperienceBackgroundColor(Color color)
    {
        experienceBackgroundColor  = color;
    }


    /**
     * The color that should be used as the experience bar foreground.
     *
     * @return The color that should be used as the experience bar foreground.
     */
    @Nonnull
    public Color getExperienceForegroundColor()
    {
        return experienceForegroundColor;
    }

    /**
     * Sets experience foreground color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     */
    public void setExperienceForegroundColor(float red, float green, float blue)
    {
        experienceForegroundColor = makeColor(red,green,blue);
    }



    /**
     * Sets experience foreground color.
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     * @param blue  the alpha
     */
    public void setExperienceForegroundColor(float red, float green, float blue, float alpha)
    {
        experienceForegroundColor = makeColor(red,green,blue, alpha);
    }

    /**
     * Sets experience foreground color.
     *
     * @param color the new color
     */
    public void setExperienceForegroundColor(Color color)
    {
        experienceForegroundColor  = color;
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
    public Color getExperienceSeparatorColor()
    {
        return experienceSeparatorColor;
    }


    /**
     * Sets experience foreground color.
     *
     * @param color the new color
     */
    public void setExperienceSeparatorColor(Color color)
    {
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
        return getMainTextColor();
    }

    /**
     * Creates an sRGB color with the specified red, green,
     * and blue values with in the range (0 - 255).
     * <p>
     * Uses the {@link com.avairebot.utilities.ColorUtil}
     * to produce the colors.
     *
     * @param red   The red component.
     * @param green The green component
     * @param blue  The blue component
     * @return The color with the given values.
     */
    protected Color makeColor(float red, float green, float blue)
    {
        return ColorUtil.makeColor(red, green, blue);
    }

    /**
     * Creates an sRGBA color with the specified red, green,
     * blue, and alpha values with in the range (0 - 255).
     * The alpha should be in the rage of 0 and 100.
     * <p>
     * Uses the {@link com.avairebot.utilities.ColorUtil} to
     * produce the colors.
     *
     * @param red   The red component.
     * @param green The green component.
     * @param blue  The blue component.
     * @param alpha The alpha component.
     * @return The color with the given values.
     */
    protected Color makeColor(float red, float green, float blue, float alpha)
    {
        return ColorUtil.makeColor(red, green, blue, alpha);
    }

}
