package com.avairebot.contracts.chat;

public interface PaginatorClosure {

    /**
     * Pagination closure, this method is invoked for each entry in
     * the paginator while looping over the items for the current
     * page via the {@link Paginator#forEach(PaginatorClosure)} method.
     *
     * @param index The index of the entry.
     * @param key   The key for the item.
     * @param val   The value of the item.
     */
    void run(int index, Object key, Object val);
}
