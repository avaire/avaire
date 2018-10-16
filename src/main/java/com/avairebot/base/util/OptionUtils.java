/*
 * OptionUtils.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.avairebot.base.util;

import java.util.ArrayList;
import java.util.Collection;

import com.avairebot.base.Option;
import com.avairebot.base.Option;

/**
 * Handles options within plugin methods. Likely to be replaced by 
 * <a href="http://jcores.net">jCores</a>.
 * 
 * @author Ralf Biedert
 * 
 * @param <T> Type paramter.
 */
public class OptionUtils<T extends Option> {

    /** Options */
    private T[] options;

    /**
     * Creates a new options array.
     * 
     * @param options
     */
    public OptionUtils(T... options) {
        this.options = options;
    }

    /**
     * Check if the selection option is available
     * 
     * @param option
     * @return .
     */
    public boolean contains(Class<? extends T> option) {
        for (T t : this.options) {
            if (option.isAssignableFrom(t.getClass())) return true;
        }

        return false;
    }

    /**
     * Check if the selection option is available
     * 
     * @param option
     * @return .
     */
    public boolean containsAny(Class<? extends T>... option) {
        for (T t : this.options) {
            for (Class<? extends T> cls : option) {
                if (cls.isAssignableFrom(t.getClass())) return true;
            }
        }

        return false;
    }

    /**
     * Returns the selection option
     * 
     * @param <O> 
     * 
     * @param option
     * @param deflt 
     * @return .
     */
    @SuppressWarnings("unchecked")
    public <O extends T> O get(Class<? extends O> option, O... deflt) {
        for (T t : this.options) {
            if (option.isAssignableFrom(t.getClass())) return (O) t;
        }

        // return the default if there is any
        if (deflt.length > 0) return deflt[0];

        return null;
    }

    /**
     * Returns the selection option
     * 
     * @param <O> 
     * 
     * @param option
     * @return .
     */
    @SuppressWarnings("unchecked")
    public <O extends T> Collection<O> getAll(Class<? extends O> option) {

        final Collection<O> rval = new ArrayList<O>();

        for (T t : this.options) {
            if (option.isAssignableFrom(t.getClass())) {
                rval.add((O) t);
            }
        }

        return rval;
    }

    /**
     * Returns the selection option
     * 
     * @param <O> 
     * 
     * @param option
     * @param handler 
     * 
     */
    @SuppressWarnings("unchecked")
    public <O extends T> void handle(Class<? extends O> option, OptionHandler<O> handler) {
        for (T t : this.options) {
            if (option.isAssignableFrom(t.getClass())) {
                handler.handle((O) t);
            }
        }
    }
}
