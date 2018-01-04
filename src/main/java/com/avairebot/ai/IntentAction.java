package com.avairebot.ai;

public class IntentAction {

    private final String action;
    private final boolean wildcard;

    public IntentAction(String action) {
        this.wildcard = action.contains(".*");
        this.action = wildcard ? action.substring(0, action.length() - 1) : action;
    }

    public String getAction() {
        return action;
    }

    public boolean isWildcard() {
        return wildcard;
    }
}
