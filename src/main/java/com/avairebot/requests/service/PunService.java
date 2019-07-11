/*
 * Copyright (c) 2019.
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
