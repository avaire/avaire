package com.avairebot.orion.contracts.chat;

import com.avairebot.orion.utilities.NumberUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Paginator(List<?> items, int perPage, String currentPage) {
        this(items, perPage, NumberUtil.parseInt(currentPage, 1));
    }

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

    public final Map<Object, Object> getItems() {
        return items;
    }

    public final int getPerPage() {
        return perPage;
    }

    public final int getCurrentPage() {
        return currentPage;
    }

    public final void setCurrentPage(int currentPage) {
        this.currentPage = Math.max(1, Math.min(getPages(), currentPage));
    }

    public final int getPages() {
        return (int) Math.ceil((double) getTotal() / perPage);
    }

    public final int getTotal() {
        return items.size();
    }
}
