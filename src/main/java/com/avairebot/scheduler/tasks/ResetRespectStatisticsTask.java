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

package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.commands.fun.RipCommand;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.time.Carbon;

public class ResetRespectStatisticsTask implements Task {

    private int currentDay = 0;

    @Override
    public void handle(AvaIre avaire) {
        if (!isSameDay()) {
            RipCommand.respect = 0;
        }
    }

    private boolean isSameDay() {
        if (Carbon.now().getDayOfYear() == currentDay) {
            return true;
        }

        currentDay = Carbon.now().getDayOfYear();
        return false;
    }
}
