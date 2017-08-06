package com.avairebot.orion.logger;

import com.avairebot.orion.Orion;

public abstract class AbstractLogger {

    protected final Orion orion;

    public AbstractLogger(Orion orion) {
        this.orion = orion;
    }

    public void info(String message) {
        this.log(Level.INFO, message);
    }

    public void info(String message, Object... args) {
        this.log(Level.INFO, String.format(message, args));
    }

    public void warning(String message) {
        this.log(Level.WARNING, message);
    }

    public void warning(String message, Object... args) {
        this.log(Level.WARNING, String.format(message, args));
    }

    public void error(String message) {
        this.log(Level.ERROR, message);
    }

    public void error(String message, Object... args) {
        this.log(Level.ERROR, String.format(message, args));
    }

    public void exception(Exception ex) {
        this.log(Level.EXCEPTION, ex.getMessage(), ex);
    }

    public void exception(String message, Exception ex) {
        this.log(Level.EXCEPTION, message, ex);
    }

    public abstract void log(Level level, String message);

    public abstract void log(Level level, String message, Exception ex);
}
