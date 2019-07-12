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

package com.avairebot.imagegen.colors.ranks;

import com.avairebot.contracts.imagegen.BackgroundRankColors;

import javax.annotation.Nonnull;
import java.awt.*;

public class AbstractColors extends BackgroundRankColors {

    @Nonnull
    @Override
    public Color getBackgroundColor() {
        return makeColor(0, 0, 0, 0);
    }

    @Nonnull
    @Override
    public Color getMainTextColor() {
        return makeColor(255, 255, 255, 100);
    }

    @Nonnull
    @Override
    public Color getSecondaryTextColor() {
        return makeColor(255, 255, 255, 100);
    }

    @Nonnull
    @Override
    public Color getExperienceBackgroundColor() {
        return makeColor(0, 0, 0, 25);
    }

    @Nonnull
    @Override
    public Color getExperienceForegroundColor() {
        return makeColor(25, 25, 25, 65);
    }

    @Nonnull
    @Override
    public Color getExperienceSeparatorColor() {
        return makeColor(255, 255, 255, 50);
    }
}
