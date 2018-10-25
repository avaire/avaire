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

    @Nonnull
    public Color getBackgroundColor() {
        return makeColor(50, 54, 60);
    }

    @Nullable
    public Color getBackgroundCoverColor() {
        return null;
    }

    @Nonnull
    public abstract Color getMainTextColor();

    @Nonnull
    public abstract Color getSecondaryTextColor();

    @Nonnull
    public abstract Color getExperienceBackgroundColor();

    @Nonnull
    public abstract Color getExperienceForegroundColor();

    @Nonnull
    public abstract Color getExperienceSeparatorColor();

    @Nonnull
    public Color getExperienceTextColor() {
        return getMainTextColor();
    }
}
