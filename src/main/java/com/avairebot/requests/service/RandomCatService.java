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
