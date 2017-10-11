package com.avairebot.orion.chat;

import com.avairebot.orion.contracts.chat.Paginator;

import java.util.List;
import java.util.Map;

public class SimplePaginator extends Paginator {

    public SimplePaginator(Map<?, ?> items, int perPage, int currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(Map<?, ?> items, int perPage, String currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(List<?> items, int perPage, int currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(List<?> items, int perPage, String currentPage) {
        super(items, perPage, currentPage);
    }

    public String generateFooter(String command) {
        return String.format("Page **%s** out of **%s** pages.\n`%s [page]`",
                getCurrentPage(),
                getPages(),
                command
        );
    }
}
