/*
 * InjectHandler.java
 * 
 * Copyright (c) 2010, Ralf Biedert All rights reserved.
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
package com.avairebot.base.impl.spawning.handler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.avairebot.base.BackendPluginManager;
import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.options.getplugin.OptionCapabilities;
import com.avairebot.base.Plugin;

public class InjectHandler extends AbstractHandler {

    /**
     * @param backendPluginManager
     */
    public InjectHandler(BackendPluginManager backendPluginManager) {
        super(backendPluginManager);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.avairebot.base.impl.spawning.handler.AbstractHandler#init(com.avairebot
     * .base.Plugin)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void init(Plugin plugin) throws Exception {

        // All fields we have a look at
        final Field[] fields = plugin.getClass().getFields();
        final Method[] methods = plugin.getClass().getMethods();

        // Process every field
        for (final Field field : fields) {
            // Try to get inject annotation. New: also turn on extended accessibility, so
            // elements don't have to be public anymore.
            field.setAccessible(true);
            final InjectPlugin ipannotation = field.getAnnotation(InjectPlugin.class);

            // If there is one ..
            if (ipannotation != null) {

                // Obtain capabilities
                final String[] capabilities = ipannotation.requiredCapabilities();

                // Handle the plugin-parameter part
                // In the default case do an auto-detection ...
                final Class<? extends Plugin> typeOfField = (Class<? extends Plugin>) field.getType();

                this.logger.fine("Injecting plugin by autodetection (" + typeOfField.getName() + ") into " + plugin.getClass().getName());

                field.set(plugin, this.backendPluginManager.getPlugin(typeOfField, new OptionCapabilities(capabilities)));
            }
        }

        // And setter methods as well (aka Scala hack)
        for (Method method : methods) {
            // Try to get inject annotation. New: also turn on extended accessibility, so
            // elements don't have to be public anymore.
            method.setAccessible(true);
            final InjectPlugin ipannotation = method.getAnnotation(InjectPlugin.class);

            if (ipannotation != null) {

                // Obtain capabilities
                final String[] capabilities = ipannotation.requiredCapabilities();

                // Handle the plugin-parameter part
                // In the default case do an auto-detection ...
                final Class<? extends Plugin> typeOfMethod = (Class<? extends Plugin>) method.getParameterTypes()[0];

                this.logger.fine("Injecting plugin by autodetection (" + typeOfMethod.getName() + ") into " + plugin.getClass().getName());

                try {
                    method.invoke(plugin, this.backendPluginManager.getPlugin(typeOfMethod, new OptionCapabilities(capabilities)));
                } catch (IllegalArgumentException e) {
                    this.logger.warning("Unable to inject plugin " + typeOfMethod + " into method " + method);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    this.logger.warning("Unable to inject plugin " + typeOfMethod + " into method " + method);
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.avairebot.base.impl.spawning.handler.AbstractHandler#deinit(com.avairebot
     * .base.Plugin)
     */
    @Override
    public void deinit(Plugin plugin) throws Exception {
        // TODO Auto-generated method stub

    }

}
