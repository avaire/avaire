package com.avairebot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

    public static Logger getLogger() {
        return LOGGER;
    }
}
