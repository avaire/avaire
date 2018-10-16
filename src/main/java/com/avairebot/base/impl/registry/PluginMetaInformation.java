/*
 * PluginMetaInformation.java
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
package com.avairebot.base.impl.registry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.avairebot.base.Plugin;

/**
 * Meta information of the given plugin.
 * 
 * @author Ralf Biedert
 */
public class PluginMetaInformation {

    /** Handles PluginLoaded annotations */
    public static class PluginLoadedInformation {
        /** Annotated method */
        public Method method;

        /** Base type to call with */
        public Class<? extends Plugin> baseType;

        /** Items already put into the method */
        public List<Plugin> calledWith = new ArrayList<Plugin>();
    }

    /**
     * @author rb
     */
    public static enum PluginStatus {
        /** No further information is available */
        UNDEFINED,

        /** Plugin has been spawned, i.e., constructed. */
        SPAWNED,

        /** Plugin has been initialized and all threads and timers have been spawned */
        INITIALIZED,

        /**
         * Plugin is currently running (default state when
         * everything is okay)
         */
        ACTIVE,

        /** Plugin was shut down. */
        TERMINATED,

        /** Plugin failed to initialize due to own report. */
        FAILED
    }

    /** Status of the plugin */
    public PluginStatus pluginStatus = PluginStatus.UNDEFINED;

    /** Meta information of the parent class */
    public PluginClassMetaInformation classMeta = null;

    /** List of declared threads, managed by the Spawner */
    public final List<Thread> threads = new ArrayList<Thread>();

    /** List of declared timer tasks, managed by the Spawner */
    public final List<TimerTask> timerTasks = new ArrayList<TimerTask>();

    /** List of declared timer tasks, managed by the Spawner */
    public final List<Timer> timers = new ArrayList<Timer>();

    /** Handles plugin loaded information */
    public final List<PluginLoadedInformation> pluginLoadedInformation = new ArrayList<PluginLoadedInformation>();

    /** Time this pluggable has been spawned. */
    public long spawnTime;

}
