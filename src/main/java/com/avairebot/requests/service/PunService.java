package com.avairebot.requests.service;

import java.util.List;

public class PunService
{
    private List<Pun> results;

    private int page;

    private int limit;

    public int getNext_page() {
        return next_page;
    }

    public int getPrevious_page() {
        return previous_page;
    }

    private int next_page;

    private int previous_page;

    public int getCurrent_page() {
        return current_page;
    }

    private int current_page;


    public int getStatus() {
        return status;
    }

    private int status;

    public int getTotal_jokes() {
        return total_jokes;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    private int total_jokes;

    private int total_pages;


    public List<Pun> getResults() {
        return results;
    }

    public boolean hasData() {
        return results != null && !results.isEmpty();
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    public class Pun
    {
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
