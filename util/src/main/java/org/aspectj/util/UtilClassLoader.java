/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Isberg        initial implementation 
 * ******************************************************************/

package org.aspectj.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Load classes as File from File[] dirs or URL[] jars.
 */
public class UtilClassLoader extends URLClassLoader {

    /** seek classes in dirs first */
    List<File> dirs;

    /** save URL[] only for toString */
    private URL[] urlsForDebugString;
    
    public UtilClassLoader(URL[] urls, File[] dirs) {
        super(urls);
        LangUtil.throwIaxIfNotAssignable(dirs, File.class, "dirs");
        this.urlsForDebugString = urls;
        List<File> dcopy = new ArrayList<>();
        
        if (!LangUtil.isEmpty(dirs)) {
            dcopy.addAll(Arrays.asList(dirs));
        }
        this.dirs = Collections.unmodifiableList(dcopy);
    }

    
    public URL getResource(String name) {
        return ClassLoader.getSystemResource(name);
    }
    
    public InputStream getResourceAsStream(String name) {
        return ClassLoader.getSystemResourceAsStream(name);
    } 
    
    public synchronized Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        // search the cache, our dirs (if maybe test), 
        // the system, the superclass (URL[]),
        // and our dirs again (if not maybe test)
        ClassNotFoundException thrown = null;
        Class<?> result =  findLoadedClass(name);
        if (null != result) {
            resolve = false;
        } else {
            try { 
                result = findSystemClass(name); 
            } catch (ClassNotFoundException e) { 
                thrown = e; 
            }
        }
        if (null == result) {
            try {
                result = super.loadClass(name, resolve);
            } catch (ClassNotFoundException e) {
                thrown = e;
            }
            if (null != result) { // resolved by superclass
                return result; 
            }
        }
        if (null == result) {
            byte[] data = readClass(name);
            if (data != null) {
                result = defineClass(name, data, 0, data.length);
            } // handle ClassFormatError?            
        }
        
        if (null == result) {
            throw (null != thrown ? thrown : new ClassNotFoundException(name));
        }
        if (resolve) {
            resolveClass(result);
        }
        return result;
    }
    
    /** @return null if class not found or byte[] of class otherwise */
    private byte[] readClass(String className) throws ClassNotFoundException {
        final String fileName = className.replace('.', '/')+".class";
		for (File dir : dirs) {
			File file = new File(dir, fileName);
			if (file.canRead()) {
				return getClassData(file);
			}
		}
        return null; 
    }
        
    private byte[] getClassData(File f) {
        try {
            FileInputStream stream= new FileInputStream(f);
            ByteArrayOutputStream out= new ByteArrayOutputStream(1000);
            byte[] b= new byte[4096];
            int n;
            while ((n= stream.read(b)) != -1) {
                out.write(b, 0, n);
            }
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }
    
    /** @return String with debug info: urls and classes used */
    public String toString() {
        return "UtilClassLoader(urls=" 
            + Arrays.asList(urlsForDebugString)
            + ", dirs="
            + dirs
            + ")";
    }
}

