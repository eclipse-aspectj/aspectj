/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)         	initial implementation
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
 *******************************************************************************/
package org.aspectj.weaver.tools.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Useful &quot;common&quot; functionality for caching to files 
 */
public abstract class AbstractFileCacheBacking extends AbstractCacheBacking {
	/**
	 * Default property used to specify a default weaving cache dir location
	 */
	public static final String WEAVED_CLASS_CACHE_DIR = "aj.weaving.cache.dir";
	private final File cacheDirectory;

	protected AbstractFileCacheBacking (File cacheDirectory) {
        if ((this.cacheDirectory=cacheDirectory) == null) {
            throw new IllegalStateException("No cache directory specified");
        }
	}

    public File getCacheDirectory () {
        return cacheDirectory;
    }

    protected void writeClassBytes (String key, byte[] bytes) throws Exception {
        File    dir=getCacheDirectory(), file=new File(dir, key);
        FileOutputStream    out=new FileOutputStream(file);
        try {
            out.write(bytes);
        } finally {
        	close(out, file);
        }
    }

	protected void delete(File file) {
		if (file.exists() && (!file.delete())) {
			if ((logger != null) && logger.isTraceEnabled()) {
				logger.error("Error deleting file " + file.getAbsolutePath());
			}
		}
	}

	protected void close(OutputStream out, File file) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				if ((logger != null) && logger.isTraceEnabled()) {
					logger.error("Failed (" + e.getClass().getSimpleName() + ")"
							   + " to close write file " + file.getAbsolutePath()
							   + ": " + e.getMessage(), e);
				}
			}
		}
	}

	protected void close(InputStream in, File file) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				if ((logger != null) && logger.isTraceEnabled()) {
					logger.error("Failed (" + e.getClass().getSimpleName() + ")"
							   + " to close read file " + file.getAbsolutePath()
							   + ": " + e.getMessage(), e);
				}
			}
		}
	}
}
