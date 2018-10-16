/*
 * InformationBrokerImpl.java
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
package com.avairebot.informationbroker.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.avairebot.informationbroker.options.PublishOption;
import com.avairebot.informationbroker.options.SubscribeOption;
import com.avairebot.informationbroker.options.publish.OptionSilentPublish;
import com.avairebot.informationbroker.options.subscribe.OptionInstantRequest;
import com.avairebot.informationbroker.InformationItem;
import com.avairebot.informationbroker.options.PublishOption;
import com.avairebot.informationbroker.options.SubscribeOption;
import com.avairebot.informationbroker.options.publish.OptionSilentPublish;
import com.avairebot.informationbroker.options.subscribe.OptionInstantRequest;
import com.avairebot.base.annotations.PluginImplementation;
import com.avairebot.base.annotations.meta.Author;
import com.avairebot.base.util.OptionUtils;
import com.avairebot.informationbroker.InformationBroker;
import com.avairebot.informationbroker.InformationItem;
import com.avairebot.informationbroker.InformationListener;
import com.avairebot.informationbroker.options.PublishOption;
import com.avairebot.informationbroker.options.SubscribeOption;
import com.avairebot.informationbroker.options.publish.OptionSilentPublish;
import com.avairebot.informationbroker.options.subscribe.OptionInstantRequest;

/**
 * Nothing to see here.
 * 
 * @author Ralf Biedert
 */
@Author(name = "Ralf Biedert")
@PluginImplementation
public class InformationBrokerImpl implements InformationBroker {
    /** Stores information on a key */
    class KeyEntry {
        /** Locks access to this item */
        final Lock entryLock = new ReentrantLock();

        /** All listeners subscribed to this item */
        final Collection<InformationListener<?>> allListeners = new ArrayList<InformationListener<?>>();

        /** The current channel holder */
        Object lastItem = null;
    }

    /** Manages all information regarding a key */
    final Map<Class<? extends InformationItem<?>>, KeyEntry> items = new HashMap<Class<? extends InformationItem<?>>, KeyEntry>();

    /** Locks access to the items */
    final Lock itemsLock = new ReentrantLock();

    /** */
    final Logger logger = Logger.getLogger(this.getClass().getName());

    /*
     * (non-Javadoc)
     * 
     * @see com.avairebot.informationbroker.InformationBroker#publish(com.avairebot.
     * informationbroker.InformationItem,
     * com.avairebot.informationbroker.options.PublishOption[])
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> void publish(Class<? extends InformationItem<T>> channel, T item,
                            PublishOption... options) {
        if (channel == null || item == null) return;

        // Get our options
        final OptionUtils<PublishOption> ou = new OptionUtils<PublishOption>(options);
        final boolean silentPublish = ou.contains(OptionSilentPublish.class);

        final KeyEntry keyEntry = getKeyEntry(channel);

        // Now process entry
        try {
            keyEntry.entryLock.lock();
            keyEntry.lastItem = item;

            // Check if we should publish silently.
            if (!silentPublish) {
                for (InformationListener<?> listener : keyEntry.allListeners) {
                    try {
                        ((InformationListener<T>) listener).update(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            keyEntry.entryLock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.avairebot.informationbroker.InformationBroker#subscribe(com.avairebot
     * .informationbroker.InformationItemIdentifier,
     * com.avairebot.informationbroker.InformationListener,
     * com.avairebot.informationbroker.options.SubscribeOption[])
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> void subscribe(Class<? extends InformationItem<T>> id,
                              InformationListener<T> listener, SubscribeOption... options) {
        if (id == null || listener == null) return;

        // Get our options
        final OptionUtils<SubscribeOption> ou = new OptionUtils<SubscribeOption>(options);
        final boolean instantRequest = ou.contains(OptionInstantRequest.class);

        // Get the meta information for the requested id
        final KeyEntry keyEntry = getKeyEntry(id);

        // Now process and add the entry
        try {
            keyEntry.entryLock.lock();
            // Only add the listener if we don't have an instant request.
            if (!instantRequest) keyEntry.allListeners.add(listener);

            // If there has been a channel established, use that one
            if (keyEntry.lastItem != null) {
                ((InformationListener<Object>) listener).update(keyEntry.lastItem);
            }

        } finally {
            keyEntry.entryLock.unlock();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.avairebot.informationbroker.InformationBroker#unsubscribe(com.avairebot
     * .informationbroker.InformationListener)
     */
    @Override
    public void unsubscribe(InformationListener<?> listener) {
        if (listener == null) return;

        // Well, not the fastest and best solution given this interface.
        this.itemsLock.lock();
        try {
            final Set<Class<? extends InformationItem<?>>> keySet = this.items.keySet();
            for (Class<? extends InformationItem<?>> uri : keySet) {
                final KeyEntry keyEntry = this.items.get(uri);
                keyEntry.entryLock.lock();
                try {
                    keyEntry.allListeners.remove(listener);
                } finally {
                    keyEntry.entryLock.unlock();
                }
            }
        } finally {
            this.itemsLock.unlock();
        }
    }

    /**
     * Returns the key entry of a given ID.
     * 
     * @param id The ID to request
     * @return The key entry.
     */
    private KeyEntry getKeyEntry(Class<? extends InformationItem<?>> id) {
        KeyEntry keyEntry = null;
        this.itemsLock.lock();
        try {
            keyEntry = this.items.get(id);
            if (keyEntry == null) {
                keyEntry = new KeyEntry();
                this.items.put(id, keyEntry);
            }
        } finally {
            this.itemsLock.unlock();
        }

        return keyEntry;
    }
}
