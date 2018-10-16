/*
 * Spawner.java
 *
 * Copyright (c) 2008, Ralf Biedert All rights reserved.
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
package com.avairebot.base.impl.spawning;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimerTask;

import com.avairebot.base.annotations.Thread;
import com.avairebot.base.annotations.Timer;
import com.avairebot.base.annotations.events.Init;
import com.avairebot.base.annotations.events.PluginLoaded;
import com.avairebot.base.annotations.events.Shutdown;
import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.diagnosis.channels.tracing.SpawnerTracer;
import com.avairebot.base.impl.spawning.handler.InjectHandler;
import com.avairebot.base.util.PluginManagerUtil;
import com.avairebot.local.Diagnosis;
import com.avairebot.local.DiagnosisChannel;
import com.avairebot.local.options.StatusOption;
import com.avairebot.local.options.status.OptionInfo;
import com.avairebot.base.impl.registry.PluginClassMetaInformation;
import com.avairebot.base.impl.spawning.handler.InjectHandler;
import com.avairebot.base.Plugin;
import com.avairebot.base.annotations.Thread;
import com.avairebot.base.annotations.Timer;
import com.avairebot.base.annotations.events.Init;
import com.avairebot.base.annotations.events.PluginLoaded;
import com.avairebot.base.annotations.events.Shutdown;
import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.diagnosis.channels.tracing.SpawnerTracer;
import com.avairebot.base.impl.PluginManagerImpl;
import com.avairebot.base.impl.registry.PluginClassMetaInformation.Dependency;
import com.avairebot.base.impl.registry.PluginMetaInformation;
import com.avairebot.base.impl.registry.PluginMetaInformation.PluginLoadedInformation;
import com.avairebot.base.impl.registry.PluginMetaInformation.PluginStatus;

/**
 * Spawn a given class.
 * 
 * @author Ralf Biedert
 */
public class Spawner {
    /** */
    //final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Used for diagnosic messages */
    DiagnosisChannel<String> diagnosis;

    /** Main plugin manager */
    private final PluginManagerImpl pluginManager;

    /**
     * Creates a new spawner with the given BackendPluginManager.
     * 
     * @param pmi
     */
    public Spawner(final PluginManagerImpl pmi) {
        this.pluginManager = pmi;
    }

    /**
     * Destroys a given plugin, halt all timers and threads, calls shutdown methods.
     * 
     * @param plugin
     * @param metaInformation
     */
    public void destroyPlugin(final Plugin plugin,
                                 final PluginMetaInformation metaInformation) {

        log("destroy/start", new OptionInfo("plugin", plugin.getClass().getCanonicalName()));
        
        // Halt all timer tasks
        for (final TimerTask timerTask : metaInformation.timerTasks) {
            timerTask.cancel();
        }

        // Halt all timers
        for (final java.util.Timer timer : metaInformation.timers) {
            timer.cancel();
        }

        // Halt all threads
        for (final java.lang.Thread thread : metaInformation.threads) {
            // TODO: Maybe not the best way to terminate.
            try {
                thread.interrupt();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        // Call shutdown hooks
        callShutdownMethods(plugin);
        log("destroy/end", new OptionInfo("plugin", plugin.getClass().getCanonicalName()));        
    }

    /**
     * Destroys a given plugin, halt all timers and threads, calls shutdown methods.
     * 
     * @param plugin
     * @param metaInformation
     */
    public void processThisPluginLoadedAnnotation(final Plugin plugin,
                                                  final PluginMetaInformation metaInformation) {

        // Get all our annotations.
        for (PluginLoadedInformation pli : metaInformation.pluginLoadedInformation) {
            final Collection<? extends Plugin> plugins = new PluginManagerUtil(this.pluginManager).getPlugins(pli.baseType);

            // For each plugin we have a request, call this plugin.
            for (Plugin p : plugins) {
                try {
                    pli.method.invoke(plugin, p);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            pli.calledWith.addAll(plugins);
        }
    }

    /**
     * Processes the {@link PluginLoaded} annotation for other plugins for this plugin.
     * 
     * @param newPlugin Newly creatd pluign
     */
    public void processOtherPluginLoadedAnnotation(Plugin newPlugin) {

        for (Plugin plugin : this.pluginManager.getPluginRegistry().getAllPlugins()) {
            final PluginMetaInformation pmi = this.pluginManager.getPluginRegistry().getMetaInformationFor(plugin);

            for (PluginLoadedInformation pli : pmi.pluginLoadedInformation) {
                final Collection<? extends Plugin> plins = new PluginManagerUtil(this.pluginManager).getPlugins(pli.baseType);

                // Check if the new plugin is returned upon request
                if (plins.contains(newPlugin)) {
                    try {
                        pli.method.invoke(plugin, newPlugin);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    pli.calledWith.add(newPlugin);
                }
            }
        }
    }

    /**
     * Spawn a plugin and process its internal annotations.
     * 
     * @param c Class to spawn from.
     * @return .
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SpawnResult spawnPlugin(final Class c) {
        log("spawn/start", new OptionInfo("plugin", c.getCanonicalName()));

        // Used for time measurements.
        final long startTime = System.nanoTime();
        final java.util.Timer timer = new java.util.Timer();
        final TimerTask lateMessage = new TimerTask() {
            @Override
            public void run() {
                log("spawn/timeout/toolong", new OptionInfo("plugin", c.getCanonicalName()));
            }
        };

        // Finally load and register plugin
        try {
            // Schedule late message. (TODO: Make this configurable)
            timer.schedule(lateMessage, 250);

            // Instanciate the plugin
            final Plugin spawnedPlugin = (Plugin) c.newInstance();

            // In here spawning of the plugin worked
            final SpawnResult spawnResult = new SpawnResult(spawnedPlugin);
            spawnResult.metaInformation.pluginStatus = PluginStatus.SPAWNED;
            spawnResult.metaInformation.spawnTime = System.currentTimeMillis();

            // Finally load and register plugin
            try {

                new InjectHandler(this.pluginManager).init(spawnedPlugin);

                // Obtain all methods
                final Method[] methods = getMethods(c);

                // 2. Call all init methods
                final boolean initStatus = callInitMethods(spawnedPlugin, methods);
                if (initStatus == false) {
                    spawnResult.metaInformation.pluginStatus = PluginStatus.FAILED;
                    return spawnResult;
                }

                // Initialization complete
                spawnResult.metaInformation.pluginStatus = PluginStatus.INITIALIZED;

                // 3. Spawn all threads
                spawnThreads(spawnResult, methods);

                // 4. Spawn timer
                spawnTimer(spawnResult, methods);

                // 5. Obtain PluginLoaded methods
                obtainPluginLoadedMethods(spawnResult, methods);

                // Currently running
                spawnResult.metaInformation.pluginStatus = PluginStatus.ACTIVE;

                log("spawn/end", new OptionInfo("plugin", c.getCanonicalName()));        
                return spawnResult;
            } catch (final Throwable e) {
                log("spawn/exception/init", new OptionInfo("plugin", c.getCanonicalName()));
                e.printStackTrace();
                Throwable cause = e.getCause();
                while (cause != null) {
                    cause.printStackTrace();
                    cause = cause.getCause();
                }
            }
            return null;

        } catch (final Throwable e) {
            log("spawn/exception/construct", new OptionInfo("plugin", c.getCanonicalName()));
            e.printStackTrace();
            Throwable cause = e.getCause();
            while (cause != null) {
                cause.printStackTrace();
                cause = cause.getCause();
            }
        } finally {
            // Halt the late message
            timer.cancel();

            final long stopTime = System.nanoTime();
            final long delta = (stopTime - startTime) / 1000;
            log("spawn/duration", new OptionInfo("plugin", c.getCanonicalName()), new OptionInfo("time", ""+ delta));
        }
        
        log("spawn/end/abnormal", new OptionInfo("plugin", c.getCanonicalName()));        
        return null;
    }

    /**
     * 
     * @param methods
     * @returns True if initialization was successful.
     * @throws IllegalAccessException
     * 
     * 
     */
    private boolean callInitMethods(final Plugin spawnedPlugin, final Method[] methods)
                                                                                          throws IllegalAccessException {
        log("callinit/start", new OptionInfo("plugin", spawnedPlugin.getClass().getCanonicalName()));        
        // final Class<? extends Plugin> spawnClass = spawnedPlugin.getClass();


        for (final Method method : methods) {
            log("callinit/method", new OptionInfo("method", method.getName()));        

            // Init methods will be marked by the corresponding annotation.
            final Init annotation = method.getAnnotation(Init.class);
            if (annotation != null) {
                log("callinit/method/initannotation", new OptionInfo("method", method.getName()));        

                try {
                    final Object invoke = method.invoke(spawnedPlugin, new Object[0]);
                    if (invoke != null && invoke instanceof Boolean) {
                        // Check if any init method returns false.
                        if (((Boolean) invoke).booleanValue() == false) return false;
                    }
                } catch (final IllegalArgumentException e) {
                    log("callinit/exception/illegalargument", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                    log("callinit/end/abnormal", new OptionInfo("plugin", spawnedPlugin.getClass().getCanonicalName()));                            
                    e.printStackTrace();
                    return false;
                } catch (final InvocationTargetException e) {
                    log("callinit/exception/invocationtargetexception", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                    log("callinit/end/abnormal", new OptionInfo("plugin", spawnedPlugin.getClass().getCanonicalName()));                            
                    e.printStackTrace();
                    return false;
                } catch (final Exception e) {
                    log("callinit/exception/exception", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                    log("callinit/end/abnormal", new OptionInfo("plugin", spawnedPlugin.getClass().getCanonicalName()));                                                
                    e.printStackTrace();
                    return false;
                }
            }
        }
        
        log("callinit/end", new OptionInfo("plugin", spawnedPlugin.getClass().getCanonicalName()));                                    
        return true;
    }

    /**
     * @param plugin
     */
    private void callShutdownMethods(final Plugin plugin) {
        log("callshutdown/start", new OptionInfo("plugin", plugin.getClass().getCanonicalName()));                
        final Class<? extends Plugin> spawnClass = plugin.getClass();
        final Method[] methods = spawnClass.getMethods();


        for (final Method method : methods) {
            log("callshutdown/method", new OptionInfo("method", method.getName()));        

            // Init methods will be marked by the corresponding annotation.
            final Shutdown annotation = method.getAnnotation(Shutdown.class);
            if (annotation != null) {
                log("callshutdown/method/shutdownannotation", new OptionInfo("method", method.getName()));        

                try {
                    method.invoke(plugin, new Object[0]);
                } catch (final IllegalArgumentException e) {
                    log("callshutdown/exception/illegalargument", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                    e.printStackTrace();
                } catch (final InvocationTargetException e) {
                    log("callinit/exception/invocationtargetexception", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                    e.printStackTrace();
                } catch (final Exception e) {
                    log("callshutdown/exception/exception", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                    e.printStackTrace();
                }
            }
        }
        
        log("callshutdown/end", new OptionInfo("plugin", plugin.getClass().getCanonicalName()));                                    
        return;
    }

    /**
     * @param c
     * @return
     */
    private Method[] getMethods(final Class<? extends Plugin> c) {
        final Method[] methods = c.getMethods();
        return methods;
    }

    /**
     * @param spawnResult
     * @param methods
     */
    private void spawnThreads(final SpawnResult spawnResult, final Method[] methods) {
        log("spawnthreads/start", new OptionInfo("plugin", spawnResult.plugin.getClass().getCanonicalName()));                
        
        for (final Method method : methods) {
            // Init methods will be marked by the corresponding annotation. New:
            // also turn on extended accessibility, so elements don't have to be public
            // anymore.
            method.setAccessible(true);
            final Thread annotation = method.getAnnotation(Thread.class);
            if (annotation != null) {

                final java.lang.Thread t = new java.lang.Thread(new Runnable() {

                    public void run() {
                        try {
                            // TODO: Pass kind of ThreadController as argument 1 (or any
                            // fitting argument)
                            method.invoke(spawnResult.plugin, new Object[0]);
                        } catch (final IllegalArgumentException e) {
                            log("spawnthreads/exception/illegalargument", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            log("spawnthreads/exception/illegalaccess", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                            e.printStackTrace();
                        } catch (final InvocationTargetException e) {
                            log("spawnthreads/exception/invocationtargetexception", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                            e.printStackTrace();
                        }
                    }
                });

                final String name = spawnResult.plugin.getClass().getName() + "." + method.getName() + "()";
                log("spawnthreads/threadstart", new OptionInfo("plugin", spawnResult.plugin.getClass().getCanonicalName()), new OptionInfo("threadname", name));                
                t.setName(name);
                t.setDaemon(annotation.isDaemonic());
                t.start();

                // Add timer task to list
                spawnResult.metaInformation.threads.add(t);
            }
        }
        
        log("spawnthreads/end", new OptionInfo("plugin", spawnResult.plugin.getClass().getCanonicalName()));                
    }

    /**
     * @param spawnResult
     * @param methods
     */
    @SuppressWarnings("unchecked")
    private void obtainPluginLoadedMethods(SpawnResult spawnResult, Method[] methods) {
        for (final Method method : methods) {
            // New: also turn on extended accessibility, so elements don't have to be
            // public anymore.
            method.setAccessible(true);
            final PluginLoaded annotation = method.getAnnotation(PluginLoaded.class);
            if (annotation != null) {
                final PluginLoadedInformation pli = new PluginLoadedInformation();
                final Class<?>[] parameterTypes = method.getParameterTypes();

                if (parameterTypes.length != 1) {
                    log("pluginloadedmethods/wrongnumberofparams", new OptionInfo("plugin", spawnResult.plugin.getClass().getCanonicalName()),  new OptionInfo("method", method.getName()));                
                    continue;
                }

                pli.method = method;
                pli.baseType = (Class<? extends Plugin>) parameterTypes[0];

                // And add result
                spawnResult.metaInformation.pluginLoadedInformation.add(pli);
            }
        }
        
    }

    /**
     * @param spawnResult
     * @param methods
     */
    private void spawnTimer(final SpawnResult spawnResult, final Method[] methods) {
        log("spawntimers/start", new OptionInfo("plugin", spawnResult.plugin.getClass().getCanonicalName()));                

        for (final Method method : methods) {
            // Init methods will be marked by the corresponding annotation. New: also
            // turn on extended accessibility, so elements don't have to be public
            // anymore.
            method.setAccessible(true);
            final Timer annotation = method.getAnnotation(Timer.class);
            if (annotation != null) {

                final java.util.Timer t = new java.util.Timer();

                final TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        try {
                            final Object invoke = method.invoke(spawnResult.plugin, new Object[0]);
                            if (invoke != null && invoke instanceof Boolean) {
                                if (((Boolean) invoke).booleanValue()) {
                                    t.cancel();
                                }
                            }
                        } catch (final IllegalArgumentException e) {
                            log("spawntimers/exception/illegalargument", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                            e.printStackTrace();
                        } catch (final IllegalAccessException e) {
                            log("spawntimers/exception/illegalaccessexception", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                            e.printStackTrace();
                        } catch (final InvocationTargetException e) {
                            log("spawntimers/exception/invocationtargetexception", new OptionInfo("method", method.getName()), new OptionInfo("message", e.getMessage()));        
                            e.printStackTrace();
                        }
                    }
                };

                if (annotation.timerType() == Timer.TimerType.RATE_BASED) {
                    t.scheduleAtFixedRate(tt, annotation.startupDelay(), annotation.period());
                }

                if (annotation.timerType() == Timer.TimerType.DELAY_BASED) {
                    t.schedule(tt, annotation.startupDelay(), annotation.period());
                }

                // Add timer task to list
                spawnResult.metaInformation.timerTasks.add(tt);
                spawnResult.metaInformation.timers.add(t);
            }

        }
        
        log("spawntimers/end", new OptionInfo("plugin", spawnResult.plugin.getClass().getCanonicalName()));                
    }

    /**
     * Returns the list of all dependencies the plugin has .
     * 
     * @param pluginClass
     * @return .
     */
    @SuppressWarnings("unchecked")
    public Collection<PluginClassMetaInformation.Dependency> getDependencies(Class<? extends Plugin> pluginClass) {
        final Collection<PluginClassMetaInformation.Dependency> rval = new ArrayList<PluginClassMetaInformation.Dependency>();

        // All fields we have a look at
        final Field[] fields = pluginClass.getFields();

        // Process every field
        for (final Field field : fields) {
            // Try to get inject annotation. New: also turn on extended accessibility,
            // so elements don't have to be public anymore.
            field.setAccessible(true);
            final InjectPlugin ipannotation = field.getAnnotation(InjectPlugin.class);

            // If there is one ..
            if (ipannotation == null) continue;

            // Don't recognize optional fields as dependencies.
            if (ipannotation.isOptional()) continue;

            // Obtain capabilities

            final PluginClassMetaInformation.Dependency d = new PluginClassMetaInformation.Dependency();
            d.capabilites = ipannotation.requiredCapabilities();
            d.pluginClass = (Class<? extends Plugin>) field.getType();
            d.isOptional = ipannotation.isOptional();

            rval.add(d);
        }

        return rval;
    }

    /**
     * Logs the given message.
     * 
     * @param message
     * @param options
     */
    void log(String message, StatusOption... options) {
        // Try to get the diagnosis
        if (this.diagnosis == null) {
            // Check if the diagnosis is already there
            final Diagnosis diag = this.pluginManager.getDiagnosis();
            if(diag==null) return;
            
            // If yes, get the main channel
            this.diagnosis = diag.channel(SpawnerTracer.class);
        }
        
        this.diagnosis.status(message, options);
    }
}
