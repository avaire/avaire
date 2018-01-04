package com.avairebot.chat;

import com.avairebot.contracts.chat.Paginator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimplePaginator extends Paginator {

    public SimplePaginator(Map<?, ?> items, int perPage, int currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(Map<?, ?> items, int perPage, String currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(Map<?, ?> items, int perPage) {
        super(items, perPage, 1);
    }

    public SimplePaginator(List<?> items, int perPage, int currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(List<?> items, int perPage, String currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(List<?> items, int perPage) {
        super(items, perPage, 1);
    }

    public SimplePaginator(Iterator<?> iterator, int perPage, int currentPage) {
        super(iterator, perPage, currentPage);
    }

    public SimplePaginator(Iterator<?> iterator, int perPage) {
        this(iterator, perPage, 1);
    }

    public String generateFooter(String command) {
        return String.format("Page **%s** out of **%s** pages.\n`%s [page]`",
            getCurrentPage(),
            getPages(),
            command
        );
    }
}
