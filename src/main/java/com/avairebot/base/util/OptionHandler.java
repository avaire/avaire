package com.avairebot.base.util;

import com.avairebot.base.Option;
import com.avairebot.base.Option;

/**
 * Handles options. Only used internally.
 * 
 * @author Ralf Biedert
 * 
 * @param <T> Type parameter.
 */
public interface OptionHandler<T extends Option> {
    /**
     * Called with e
     * 
     * @param option
     */
    public void handle(T option);
}
