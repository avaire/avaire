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

package com.avairebot.contracts.chat;

public interface PaginatorClosure<T> {

    /**
     * Pagination closure, this method is invoked for each entry in
     * the paginator while looping over the items for the current
     * page via the {@link Paginator#forEach(PaginatorClosure)} method.
     *
     * @param index The index of the entry.
     * @param key   The key for the item.
     * @param val   The value of the item.
     */
    void run(int index, Object key, T val);
}
