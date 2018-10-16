/*
 * Plugin.java
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

import com.avairebot.base.annotations.PluginImplementation;
import com.avairebot.base.util.PluginUtil;

/**
 * The base class of all plugins. Plugin creation is fairly simple: <br>
 * <br>
 * 1. Create a new top level package for your plugin, e.g., 
 * <code>com.company.plugins.imagedb</code> in case of an image database.<br>
 * <br>
 * 2. Create an interface within that package. The interface should (well, must)
 * extend Plugin. Add all the methods you like, for example <code>listImages()</code><br> 
 * <br>
 * 3. Create an impl sub-package with the plugin package, in our case this is
 * <code>com.company.plugins.imagedb.impl</code>. The <i>impl</i>-name is not 
 * required, but should be kept as a convention. In the future there might exist 
 * tools that depend on it, or work better with it. If you have multiple implementations, 
 * create several sub-packages within the impl folder. In our example case, this 
 * could be the implementations <code>impl.simple</code> (for a simple test 
 * implementation), <code>impl.distributed</code> (for our distributed image storage) 
 * and <code>impl.compatiblity</code> (for the old DB API)<br>
 * <br>
 * 4. Implement your interfaces, i.e., create a class / classes inside the 
 * respective <code>impl</code> folder.<br>
 * <br>
 * 5. Add the &#064;{@link PluginImplementation} annotation to your implemented
 * class(es).<br>
 * <br>
 * 6. You're done. Technically your plugin is ready now to use. It can be compiled 
 * now (Eclipse will probably have done this for you already). You might want to 
 * have a look at the {@link BackendPluginManager} documentation to see how you can load and retrieve
 * it (see <code>addPluginsFrom()</code> and <code>getPlugin()</code>).<br>
 * <br>
 * NOTE: You should <b>ensure that all implementations of your plugins are thread 
 * safe</b>! Expect your functions to be called any time in any state.
 *
 * @author Ralf Biedert
 * @see BackendPluginManager
 * @see PluginUtil
 */
public interface Plugin {
}
