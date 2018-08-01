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
