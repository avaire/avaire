package com.avairebot.requests.service;

import java.util.List;
import java.util.Objects;

public class ChuckNorrisService {

    private String type;
    private ChuckNorrisValue value;

    public String getType() {
        return type;
    }

    public ChuckNorrisValue getValue() {
        return value;
    }

    public boolean isSuccess() {
        return Objects.equals(getType(), "success");
    }

    public class ChuckNorrisValue {
        private int id;
        private String joke;
        private List<String> categories;

        public int getId() {
            return id;
        }

        public String getJoke() {
            return joke;
        }

        public List<String> getCategories() {
            return categories;
        }
    }
}
