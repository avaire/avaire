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

    static {
        regular = loadFont(Font.TRUETYPE_FONT, "fonts/Poppins-Regular.ttf");
        medium = loadFont(Font.TRUETYPE_FONT, "fonts/Poppins-Medium.ttf");
        bold = loadFont(Font.TRUETYPE_FONT, "fonts/Poppins-Bold.ttf");
    }

    private static Font loadFont(int type, String resourceName) {
        try {
            return Font.createFont(type, Fonts.class.getClassLoader().getResourceAsStream(resourceName));
        } catch (FontFormatException | IOException e) {
            throw new FailedToLoadResourceException(String.format("Failed to load the font resource %s",
                resourceName
            ), e);
        }
    }
}
