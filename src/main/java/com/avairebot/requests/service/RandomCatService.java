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

public class RandomCatService {

    private Data data;

    public Data getData() {
        return data;
    }

    public class Data {
        private String description;
        private String type;
        private String file;
        private String url;

        public Data(String description, String type, String file, String url) {
            this.description = description;
            this.type = type;
            this.file = file;
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public String getFile() {
            return file;
        }

        public String getUrl() {
            return url;
        }
    }
}
