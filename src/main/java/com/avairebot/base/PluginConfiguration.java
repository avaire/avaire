/*
 * PluginConfiguration.java
 *
 * Copyright (c) 2007, Ralf Biedert All rights reserved.
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
package com.avairebot.base;

import com.avairebot.base.annotations.configuration.ConfigurationFile;
import com.avairebot.base.impl.PluginManagerFactory;
import com.avairebot.base.util.JSPFProperties;
import com.avairebot.base.util.PluginConfigurationUtil;
import com.avairebot.base.impl.PluginManagerFactory;
import com.avairebot.base.annotations.configuration.ConfigurationFile;
import com.avairebot.base.impl.PluginManagerFactory;
import com.avairebot.base.util.JSPFProperties;
import com.avairebot.base.util.PluginConfigurationUtil;

/**
 * Gives you access to configuration items of plugins. In general there are three ways 
 * of adding configuration: 
 * <ol>
 * <li>by calling <code>setPreferences()</code></li>
 * <li>by providing a {@link JSPFProperties} object to the {@link PluginManagerFactory}</li>
 * <li>by using the &#064;{@link ConfigurationFile} annotation.<br/><br/></li>
 * </ol>
 * 
 * @author Ralf Biedert
 * @see PluginConfigurationUtil
 */
public interface PluginConfiguration extends Plugin {
    /**
     * Gets a configuration key. Root may be added for convenience and will
     * prefix the subkey with its fully qualified name. Thus, if there is an interface
     * <code>GeoService</code> in the package <code>com.company.plugins.geoservice</code>
     * the following call:<br/><br/>  
     * <code>
     * getConfiguration(GeoService.class, "remote.url")
     * </code><br/><br/>
     * would try to return the configuration key <code>com.company.plugins.geoservice.GeoService.remote.url</code>.
     *
     * @param root May also be null.
     * @param subkey If used in conjunction with root it should not be prefixed
     * with a dot (".")
     *
     * @return The corresponding value or null if nothing was found.
     */
    public String getConfiguration(Class<?> root, String subkey);

    /**
     * Set the key for a value. Root may be added for convenience and will
     * prefix the subkey with its FQN. Usually the configuration is added 
     * by providing JSPFProperties object to the {@link PluginManagerFactory} 
     *
     * @param root May also be null.
     * @param subkey If used in conjunction with root it should not be prefixed 
     * with a dot (".")
     * @param value The value to set.
     */
    public void setConfiguration(Class<?> root, String subkey, String value);

}
