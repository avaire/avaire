/*
 * PluginManager.java
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

import java.net.URI;

import com.avairebot.base.annotations.PluginImplementation;
import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.impl.PluginManagerFactory;
import com.avairebot.base.options.AddPluginsFromOption;
import com.avairebot.base.options.GetPluginOption;
import com.avairebot.base.util.JSPFProperties;
import com.avairebot.base.util.PluginManagerUtil;
import com.avairebot.base.util.uri.ClassURI;
import com.avairebot.informationbroker.InformationBroker;
import com.avairebot.base.impl.PluginManagerFactory;
import com.avairebot.base.annotations.PluginImplementation;
import com.avairebot.base.annotations.injections.InjectPlugin;
import com.avairebot.base.impl.PluginManagerFactory;
import com.avairebot.base.options.AddPluginsFromOption;
import com.avairebot.base.options.GetPluginOption;
import com.avairebot.base.util.JSPFProperties;
import com.avairebot.base.util.PluginManagerUtil;
import com.avairebot.base.util.uri.ClassURI;
import com.avairebot.informationbroker.InformationBroker;

/**
 * This is your entry point to and the heart of JSPF. The plugin manager keeps track of all 
 * registed plugins and gives you methods to add and query them. You cannot instantiate the
 * BackendPluginManager directly, instead you<br/><br/>
 * 
 * <center>create this class the first time by a call to {@link PluginManagerFactory}<code>.createPluginManager()</code></center>
 * 
 * <br/><br/>
 * Afterwards you probably want to add some of your own plugins. During the lifetime of your 
 * application there should only be one BackendPluginManager. The BackendPluginManager does not have to be
 * passed to the inside of your plugins, instead, they can request it by the &#064;{@link InjectPlugin}
 * annotation (i.e, create a field '<code>public BackendPluginManager manager</code>' and add the
 * annotation).<br/><br/>
 * 
 * There are also a number of default plugins you can retrieve right away (without calling 
 * <code>addPluginsFrom()</code> explicitly.). These are:<br/><br/>
 *  
 *  <ul>
 *  <li>{@link PluginConfiguration} - enables you to get and set config options</li>
 *  <li>{@link PluginInformation} - provides meta information about plugins</li>
 *  <li>{@link InformationBroker} - supports information exchange between plugins while keeping them decoupled</li>
 *  </ul><br/>
 *  
 * In addition (and after loading the specific plugin) you can also retrieve a RemoteAPI.   
 * <br/><br/>
 *    
 * The following configuration sub-keys are usually known for this class (see {@link PluginConfiguration}, keys must be set 
 * <em>before</em> createPluginManager() is being called, i.e., set in the {@link JSPFProperties} object!):<br/><br/>
 * 
 *  <ul>
 *  <li><b>cache.enabled</b> - Specifies if the known plugins should be cached. Specify either {true, false}.</li>
 *  <li><b>cache.mode</b> - If we should use strong caching (slow but more accurate) or weak (much faster). Specify either {stong, weak}. </li>
 *  <li><b>cache.file</b> - Cache file to use. Specify any relative or absolute file path, file will be created / overwritten.</li>
 *  <li><b>classpath.filter.default.enabled</b> - If Java default classpaths (e.g., jre/lib/*) should be filtered. Specify either {true, false}. Might not work on all platforms as expected.</li>
 *  <li><b>classpath.filter.default.pattern</b> - Specify what to filter in addition to default classpaths. Specify a list of ';' separated tokens, e.g., "jdk/lib;jre/lib". Will be matched against URL representations, so all \\ will be converted to / (and ' ' might become %20, ...).</li>
 *  <li><b>logging.level</b> - Either {OFF, FINEST, FINER, FINE, INFO, WARNING, ALL}. Specifies what to log on the console. </li>
 *  </ul><br/>
 * @see PluginManagerUtil
 *
 * @author Ralf Biedert
 */
public interface BackendPluginManager extends Plugin {
    /**
     * Requests the plugin manager to add plugins from a given path. The path can be
     * either a folder-like item where existing .zip and .jar files are trying to be
     * added, as well as existing class files. The path can also be a singular .zip or
     * .jar which is added as well.<br><br>
     *
     * The manager will search for classes having the &#064;{@link PluginImplementation}
     * annotation and evaluate this annotation. Thereafter the plugin will be instantiated.<br><br>
     * 
     * Currently supported are classpath-folders (containing no .JAR files), plugin folders 
     * (containing .JAR files or multiplugins), single plugins and HTTP locations. Example
     * calls look like this:<br/><br/>
     * 
     * <ul>
     * <li><code>addPluginsFrom(new URI("classpath://*"))</code> (add all plugins within the current classpath).</li>
     * <li><code>addPluginsFrom(new File("plugins/").toURI())</code> (all plugins from the given folder, scanning for JARs and <a href="http://code.google.com/p/jspf/wiki/FAQ">multi-plugins</a>).</li>
     * <li><code>addPluginsFrom(new File("plugin.jar").toURI())</code> (the given plugin directly, no scanning is being done).</li>
     * <li><code>addPluginsFrom(new URI("http://sample.com/plugin.jar"))</code> (downloads and adds the given plugin, use with caution).</li>
     * <li><code>addPluginsFrom(new ClassURI(ServiceImpl.class).toURI())</code> (adds the specific plugin implementation already present in the classpath; very uncomfortable, very fast).</li>
     * </ul>
     * 
     *   
     * @see ClassURI
     *
     * @param url The URL to add from. If this is "classpath://*"; the plugin manager will 
     * load all plugins within it's own classpath. 
     * 
     * @param options A set of options supported. Please see the individual options for more
     * details.
     */
    public void addPluginsFrom(URI url, AddPluginsFromOption... options);

    /**
     * Returns the next best plugin for the requested interface. The way the plugin is being 
     * selected is undefined, you should assume that a random plugin implementing the requested 
     * interface is chosen. <br><br>
     * 
     * This method is more powerful than it looks like on first sight, especially in conjunction
     * with the right {@link GetPluginOption}.
     * 
     * @param <P> Type of the plugin / return value. 
     *
     * @param plugin The interface to request. The given class <b>must</b> derive from Plugin. You <b>MUST NOT</b> pass 
     * implementation classes. Only interface classes are accepted (i.e. <code>getPlugin(Service.class)</code>
     * is fine, while <code>getPlugin(ServiceImpl.class)</code> isn't. 
     * @param options A set of options for the request.
     *
     * @return A randomly chosen Object that implements <code>plugin</code>.
     */
    public <P extends Plugin> P getPlugin(Class<P> plugin, GetPluginOption... options);

    /**
     * Tells the plugin manager to shut down. This may be useful in cases where you want all 
     * created plugins to be destroyed and shutdown hooks called. Normally this happens during 
     * application termination automatically, but sometimes you create a 2nd instance in the same 
     * machine and want the first one to close properly.   
     * 
     * All invocations after the first one have no effect.
     */
    public void shutdown();

}
