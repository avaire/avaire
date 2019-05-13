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

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public abstract class Paginator<T> implements Cloneable {

    /**
     * The items that should be paginated.
     */
    protected final LinkedHashMap<Object, T> items;

    /**
     * The amount of items to display per-page.
     */
    protected final int perPage;

    /**
     * The current page ID.
     */
    protected int currentPage;

    /**
     * Creates a new paginator instance using the given
     * items, per page, and current page values.
     *
     * @param items       The items that should be paginated.
     * @param perPage     The amount of items to show per-page.
     * @param currentPage The current page that should be shown.
     */
    public Paginator(@Nonnull Map<?, T> items, int perPage, int currentPage) {
        this.items = new LinkedHashMap<>(items);
        this.perPage = perPage;

        this.setCurrentPage(currentPage);
    }

    /**
     * Creates a new paginator instance using the given items, per page,
     * and current page, if the current page is not a valid number,
     * the first page will be selected instead.
     *
     * @param items       The items that should be paginated.
     * @param perPage     The amount of items to show per-page.
     * @param currentPage The current page that should be shown.
     */
    public Paginator(@Nonnull Map<?, T> items, int perPage, @Nonnull String currentPage) {
        this(items, perPage, NumberUtil.parseInt(currentPage, 1));
    }

    /**
     * Creates a new paginator instance using the given items list, per page,
     * and current page, the items will be converted to a map, where the
     * key for the map is the index for the record in the list element,
     * and the value will stay as the value.
     *
     * @param items       The items that should be paginated.
     * @param perPage     The amount of items to show per-page.
     * @param currentPage The current page that should be shown.
     */
    public Paginator(@Nonnull List<T> items, int perPage, int currentPage) {
        LinkedHashMap<Object, T> map = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i++) {
            map.put(i, items.get(i));
        }
        this.items = map;
        this.perPage = perPage;

        this.setCurrentPage(currentPage);
    }

    /**
     * Creates a new paginator instance using the given items list, per page,
     * and current page, the items will be converted to a map, where the
     * key for the map is the index for the record in the list element,
     * and the value will stay as the value.
     * <p>
     * The current page will be parsed to a integer, if the current page is
     * not a valid number, the first page will be selected instead.
     *
     * @param items       The items that should be paginated.
     * @param perPage     The amount of items to show per-page.
     * @param currentPage The current page that should be shown.
     */
    public Paginator(@Nonnull List<T> items, int perPage, @Nonnull String currentPage) {
        this(items, perPage, NumberUtil.parseInt(currentPage, 1));
    }

    /**
     * Creates a new paginator instance using the given iterator, per page, and
     * current page, the items will be pulled from the iterator and be stored
     * into a map, where the key is the index in the given iterator.
     *
     * @param iterator    The iterator containing all the items that should be paginated.
     * @param perPage     The amount of items to show per-page.
     * @param currentPage The current page that should be shown.
     */
    public Paginator(@Nonnull Iterator<T> iterator, int perPage, int currentPage) {
        int index = 0;
        LinkedHashMap<Object, T> items = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            items.put(index++, iterator.next());
        }

        this.items = items;
        this.perPage = perPage;

        this.setCurrentPage(currentPage);
    }


    /**
     * Loops over each item in the paginator for the current page
     * and invokes the closure with the index, key, and value.
     *
     * @param closure The closure that should get the paginator item for the current page.
     */
    public void forEach(@Nonnull PaginatorClosure<? super T> closure) {
        int counter = 0;
        int start = perPage * (getCurrentPage() - 1);
        int end = start + getPerPage();

        for (Map.Entry<Object, T> item : items.entrySet()) {
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
    public final Map<Object, T> getItems() {
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
