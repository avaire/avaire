package com.avairebot.orion.logger;

import com.avairebot.orion.Orion;

import java.util.logging.Level;

public class Logger {

    public void severe(String message) {

        java.util.logging.Logger.getLogger(Orion.class.getName()).log(Level.SEVERE, message);
    }

    public void info(String message) {
        java.util.logging.Logger.getLogger(Orion.class.getName()).log(Level.INFO, message);
    }

    public void warning(String message) {
        java.util.logging.Logger.getLogger(Orion.class.getName()).log(Level.WARNING, message);
    }

    public void exception(Exception ex) {
        java.util.logging.Logger.getLogger(Orion.class.getName()).log(Level.SEVERE, null, ex);
    }

    public void message(String message) {
        System.out.println(message);
    }
}
