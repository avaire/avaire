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

package com.avairebot.imagegen;

import com.avairebot.exceptions.FailedToLoadResourceException;

import java.awt.*;
import java.io.IOException;
import java.util.Locale;

public class Fonts {

    /**
     * The Poppins Regular font.
     * <p>
     * For more information see: https://fonts.google.com/specimen/Poppins
     */
    public static final Font regular;

    /**
     * The Poppins Medium font.
     * <p>
     * For more information see: https://fonts.google.com/specimen/Poppins
     */
    public static final Font medium;

    /**
     * The Poppins Bold font.
     * <p>
     * For more information see: https://fonts.google.com/specimen/Poppins
     */
    public static final Font bold;

    /**
     * The Poppins Extra Bold font.
     * <p>
     * For more information see: https://fonts.google.com/specimen/Poppins
     */
    public static final Font extraBold;

    static {
        boolean isWindows = System.getProperty("os.name", "generic")
            .toLowerCase(Locale.ENGLISH)
            .contains("win");

        regular = loadFont("Poppins-Regular." + (isWindows ? "ttf" : "otf"));
        medium = loadFont("Poppins-Medium." + (isWindows ? "ttf" : "otf"));
        bold = loadFont("Poppins-Bold." + (isWindows ? "ttf" : "otf"));
        extraBold = loadFont("Poppins-ExtraBold." + (isWindows ? "ttf" : "otf"));
    }

    private static Font loadFont(String resourceName) {
        try {
            return Font.createFont(
                Font.TRUETYPE_FONT,
                Fonts.class.getClassLoader()
                    .getResourceAsStream("fonts/" + resourceName)
            );
        } catch (FontFormatException | IOException e) {
            throw new FailedToLoadResourceException(String.format("Failed to load the font resource %s",
                resourceName
            ), e);
        }
    }
}
