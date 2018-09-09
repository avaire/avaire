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

package com.avairebot.contracts.commands.interactions;

import com.avairebot.utilities.RandomUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Lottery {

    private final Set<LotteryEntity> entities = new HashSet<>();

    /**
     * Creates a new lottery object with the given range of indexes to create.
     *
     * @param range The range of indexes to create.
     */
    public Lottery(int range) {
        for (int i = 0; i < range; i++) {
            entities.add(new LotteryEntity(i));
        }
    }

    /**
     * Picks a winner from the lottery entities by creating a pool
     * with every entries tickets and picking one at random.
     *
     * @return The winners index ID.
     */
    public int getWinner() {
        int poolRange = 0;
        Map<Integer, LotteryEntity> pool = new HashMap<>();
        for (LotteryEntity entity : entities) {
            entity.tickets += 1;
            for (int i = 0; i < entity.tickets; i++) {
                pool.put(poolRange++, entity);
            }
        }

        int integer = RandomUtil.getInteger(poolRange - 1);
        LotteryEntity entity = pool.get(integer);
        entity.tickets = 0;

        return entity.id;
    }

    /**
     * The lottery entity, the entity holds the lottery ID(Index)
     * and the amount of tickets that each entity has.
     */
    private class LotteryEntity {

        private final int id;
        private int tickets = 10;

        LotteryEntity(int id) {
            this.id = id;
        }
    }
}
