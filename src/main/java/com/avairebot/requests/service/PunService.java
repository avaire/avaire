package com.avairebot.requests.service;

import java.util.List;

public class PunService {

    private List<Pun> results;
    private int page;
    private int limit;
    private int next_page;
    private int previous_page;
    private int current_page;
    private int status;
    private int total_jokes;
    private int total_pages;

    public boolean hasData() {
        return results != null && !results.isEmpty();
    }

    public int getNextPage() {
        return next_page;
    }

    public int getPreviousPage() {
        return previous_page;
    }

    public int getCurrentPage() {
        return current_page;
    }

    public int getStatus() {
        return status;
    }

    public int getTotalJokes() {
        return total_jokes;
    }

    public int getTotalPages() {
        return total_pages;
    }

    public List<Pun> getResults() {
        return results;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    public class Pun {

        private String id;
        private String joke;

        public String getId() {
            return id;
        }

        public String getJoke() {
            return joke;
        }
    }
}
