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

import java.awt.*;

public interface BackgroundColors {

    /**
     * Creates an sRGBA color with the specified red, green,
     * blue, and alpha values with in the range (0 - 255).
     * The alpha should be in the rage of 0 and 100.
     *
     * @param red   The red component.
     * @param green The green component.
     * @param blue  The blue component.
     * @param alpha The alpha component.
     * @return The color with the given values.
     */
    default Color makeColor(float red, float green, float blue, float alpha) {
        return new Color(red / 255F, green / 255F, blue / 255F, alpha / 100F);
    }

    /**
     * Creates an sRGB color with the specified red, green,
     * and blue values with in the range (0 - 255).
     *
     * @param red   The red component.
     * @param green The green component
     * @param blue  The blue component
     * @return The color with the given values.
     */
    default Color makeColor(float red, float green, float blue) {
        return new Color(red / 255F, green / 255F, blue / 255F, 1F);
    }
}
