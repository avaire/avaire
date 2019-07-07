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

package com.avairebot.imagegen.colors.ranks;

import com.avairebot.contracts.imagegen.BackgroundRankColors;
import com.avairebot.utilities.ColorUtil;

import javax.annotation.Nonnull;
import java.awt.*;

public class DiscordDarkColors extends BackgroundRankColors {

    @Nonnull
    @Override
    public Color getBackgroundColor() {
        return ColorUtil.makeColor(47, 49, 54);
    }

    @Nonnull
    @Override
    public Color getMainTextColor() {
        return ColorUtil.makeColor(226, 226, 229, 85);
    }

    @Nonnull
    @Override
    public Color getSecondaryTextColor() {
        return ColorUtil.makeColor(166, 166, 166, 85);
    }

    @Nonnull
    @Override
    public Color getExperienceBackgroundColor() {
        return ColorUtil.makeColor(32, 34, 37, 60);
    }

    @Nonnull
    @Override
    public Color getExperienceForegroundColor() {
        return ColorUtil.makeColor(114, 137, 218, 80);
    }

    @Nonnull
    @Override
    public Color getExperienceSeparatorColor() {
        return ColorUtil.makeColor(109, 131, 208, 80);
    }
}
