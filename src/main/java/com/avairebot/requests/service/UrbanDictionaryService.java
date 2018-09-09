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

package com.avairebot.requests.service;

import java.util.List;

public class UrbanDictionaryService {

    private List<UrbanDictionary> list;

    public List<UrbanDictionary> getList() {
        return list;
    }

    public boolean hasData() {
        return getList() != null && !getList().isEmpty();
    }

    public class UrbanDictionary {

        private int defid;
        private int thumbs_up;
        private int thumbs_down;
        private String definition;
        private String permalink;
        private String author;
        private String word;
        private String example;

        public int getDefid() {
            return defid;
        }

        public int getThumbsUp() {
            return thumbs_up;
        }

        public int getThumbsDown() {
            return thumbs_down;
        }

        public String getDefinition() {
            return definition;
        }

        public String getPermalink() {
            return permalink;
        }

        public String getAuthor() {
            return author;
        }

        public String getWord() {
            return word;
        }

        public String getExample() {
            return example;
        }
    }
}
