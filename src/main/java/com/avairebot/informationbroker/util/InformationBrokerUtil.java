/*
 * InformationBrokerUtil.java
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
package com.avairebot.informationbroker.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.avairebot.informationbroker.options.subscribe.OptionInstantRequest;
import com.avairebot.informationbroker.InformationItem;
import com.avairebot.informationbroker.options.subscribe.OptionInstantRequest;
import com.avairebot.informationbroker.InformationBroker;
import com.avairebot.informationbroker.InformationItem;
import com.avairebot.informationbroker.InformationListener;
import com.avairebot.informationbroker.options.subscribe.OptionInstantRequest;

/**
 * Helper functions for the {@link InformationBroker} interface. The util uses the
 * embedded
 * interface to provide more convenience features.
 * 
 * @author Ralf Biedert
 * @see InformationBroker
 */
public class InformationBrokerUtil {
    /** The information broker */
    private final InformationBroker broker;

    /**
     * Creates a new InformationBrokerUtil.
     * 
     * @param broker
     */
    public InformationBrokerUtil(InformationBroker broker) {
        this.broker = broker;
    }

    /**
     * Returns the value for the given id or <code>dflt</code> if neither the key
     * nor the default was present. For example, to retrieve the current user name
     * and to get "unknown" if none was present, you could write:<br/>
     * <br/>
     * 
     * <code>
     * get(new StringID("user:name"), "unknown");
     * </code><br/>
     * <br/>
     * 
     * @param <T> The type of the return value.
     * @param id The ID to request.
     * @param dflt The default value to return if no item was found.
     * @return Returns the requested item, a default if the item was not present or null
     * in case neither was found.
     */
    public <T> T get(Class<? extends InformationItem<T>> id, T... dflt) {
        final AtomicReference<T> object = new AtomicReference<T>();
        this.broker.subscribe(id, new InformationListener<T>() {
            @Override
            public void update(T item) {
                object.set(item);
            }
        }, new OptionInstantRequest());

        final T rval = object.get();

        // Now check if we have a sensible return value or not, and return the default
        // if we must
        if (rval == null && dflt.length > 0) return dflt[0];
        return rval;
    }

    /**
     * Subscribes to a number of items. The listener is called only when all items are
     * available or when all were available and a single item changed. For example, to
     * subscribe to two items you could write:<br/>
     * <br/>
     * <code>
     * subscribeAll(listener, ItemA.class, ItemB.class);
     * </code><br/>
     * <br/>
     * 
     * Use <code>get()</code> from inside the listener to obtain the specific items.
     * 
     * @param listener The listener called when all prerequisites are met. Note that the
     * listener will be called <b>without</b> any object (i.e., <code>item</code> is
     * <code>null</code>). You must use the broker's <code>get()</code> function.
     * @param all All IDs we should subscribe to.
     */
    @SuppressWarnings("unchecked")
    public void subscribeAll(final InformationListener<Void> listener,
                             final Class<?>... all) {
        if (listener == null || all == null || all.length == 0) return;

        // Stores all items we received so far
        final Map<Class<?>, AtomicReference<Object>> map = new ConcurrentHashMap<Class<?>, AtomicReference<Object>>();

        for (final Class<?> c : all) {
            final Class<? extends InformationItem<Object>> cc = (Class<? extends InformationItem<Object>>) c;

            this.broker.subscribe(cc, new InformationListener<Object>() {
                @Override
                public void update(Object item) {
                    // First update the map
                    map.put(cc, new AtomicReference<Object>(item));

                    // then check if we are complete
                    if (map.keySet().size() == all.length) {
                        listener.update(null);
                    }
                }
            });
        }
    }
}
