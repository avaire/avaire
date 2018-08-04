package com.avairebot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    public static Logger getLogger() {
        return log;
    }
}
