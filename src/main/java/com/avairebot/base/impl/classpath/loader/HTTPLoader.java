/*
 * InternalClasspathLoader.java
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
package com.avairebot.base.impl.classpath.loader;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import com.avairebot.AvaIre;
import com.avairebot.base.impl.PluginManagerImpl;

/**
 * @author rb
 *
 */
public class HTTPLoader extends FileLoader {

    /**
     * @param pluginManager
     */
    public HTTPLoader(PluginManagerImpl pluginManager, AvaIre bot) {
        super(pluginManager, bot);
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.loader.AbstractLoader#handlesURI(java.net.URI)
     */
    @Override
    public boolean handlesURI(URI uri) {
        if (uri.getScheme().equals("http")) return true;

        return false;
    }

    /* (non-Javadoc)
     * @see com.avairebot.base.impl.loader.AbstractLoader#loadFrom(java.net.URI)
     */
    @Override
    public void loadFrom(URI url) {
        // Handle http files
        if (url.getScheme().equals("http")) {

            try {
                // download the file (TODO: download could be improved a bit ...)
                java.io.File tmpFile = java.io.File.createTempFile("jspfplugindownload", ".jar");
                final FileOutputStream fos = new FileOutputStream(tmpFile);

                final URL url2 = url.toURL();
                final InputStream openStream = url2.openStream();

                int read = 0;
                byte buf[] = new byte[1024];

                while ((read = openStream.read(buf)) > 0) {
                    fos.write(buf, 0, read);
                }

                fos.close();
                openStream.close();

                // BIG FAT TODO: Do signature check!!!
                locateAllPluginsAt(tmpFile);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                this.logger.warning("Error downloading plugins from " + url);
            }
        }
    }

}
