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

import com.avairebot.utilities.NumberUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public abstract class Paginator implements Cloneable {

    protected final Map<Object, Object> items;
    protected final int perPage;
    protected int currentPage;

    public Paginator(Map<?, ?> items, int perPage, int currentPage) {
        this.items = new HashMap<>(items);
        this.perPage = perPage;

        this.setCurrentPage(currentPage);
    }

    public Paginator(Map<?, ?> items, int perPage, String currentPage) {
        this(items, perPage, NumberUtil.parseInt(currentPage, 1));
    }

    public Paginator(List<?> items, int perPage, int currentPage) {
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            map.put(i, items.get(i));
        }
        this.items = map;
        this.perPage = perPage;

        this.setCurrentPage(currentPage);
    }

    public Paginator(Iterator<?> iterator, int perPage, int currentPage) {
        int index = 0;
        Map<Object, Object> items = new HashMap<>();
        while (iterator.hasNext()) {
            items.put(index++, iterator.next());
        }

        this.items = items;
        this.perPage = perPage;

        this.setCurrentPage(currentPage);
    }

    public Paginator(List<?> items, int perPage, String currentPage) {
        this(items, perPage, NumberUtil.parseInt(currentPage, 1));
    }

    /**
     * Loops over each item in the paginator for the current page
     * and invokes the closure with the index, key, and value.
     *
     * @param closure The closure that should get the paginator item for the current page.
     */
    public void forEach(PaginatorClosure closure) {
        int counter = 0;
        int start = perPage * (getCurrentPage() - 1);
        int end = start + getPerPage();

        for (Map.Entry<Object, Object> item : items.entrySet()) {
            int current = counter;
            counter++;

            if (current < start) {
                continue;
            }

            if (current >= end || getTotal() <= current) {
                break;
            }

            closure.run(current, item.getKey(), item.getValue());
        }
    }

    /**
     * Gets a map of the items for the paginator.
     *
     * @return A map of items for the paginator.
     */
    public final Map<Object, Object> getItems() {
        return items;
    }

    /**
     * Gets the amount of items that should be displayed per page.
     *
     * @return The amount of items that should be displayed per page.
     */
    public final int getPerPage() {
        return perPage;
    }

    /**
     * Gets the current page.
     *
     * @return The current page.
     */
    public final int getCurrentPage() {
        return currentPage;
    }

    /**
     * Sets the current page that should be viewed, the the minimum being 1, and
     * the maximum being the maximum amount of pages the paginator current has.
     *
     * @param currentPage The page that should be displayed.
     */
    public final void setCurrentPage(int currentPage) {
        this.currentPage = Math.max(1, Math.min(getPages(), currentPage));
    }

    /**
     * Gets the total amount of pages for our current {@link Paginator#perPage per pages}
     * and our total amount of items.
     *
     * @return The total amount of pages for the current paginator.
     */
    public final int getPages() {
        return (int) Math.ceil((double) getTotal() / perPage);
    }

    /**
     * Gets the total amount of items current stored in the paginator.
     *
     * @return The total amount of items in the paginator.
     */
    public final int getTotal() {
        return items.size();
    }
}
