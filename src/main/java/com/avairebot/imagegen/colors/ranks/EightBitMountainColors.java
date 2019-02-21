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

public class EightBitMountainColors extends BackgroundRankColors {

    @Nonnull
    @Override
    public Color getBackgroundColor() {
        return makeColor(55, 55, 70);
    }

    @Nonnull
    @Override
    public Color getMainTextColor() {
        return makeColor(213, 209, 216, 85);
    }

    @Nonnull
    @Override
    public Color getSecondaryTextColor() {
        return makeColor(220, 191, 198, 85);
    }

    @Nonnull
    @Override
    public Color getExperienceBackgroundColor() {
        return makeColor(183, 90, 102, 75);
    }

    @Nonnull
    @Override
    public Color getExperienceForegroundColor() {
        return makeColor(156, 129, 159, 80);
    }

    @Nonnull
    @Override
    public Color getExperienceSeparatorColor() {
        return makeColor(83, 180, 201, 0);
    }
}
