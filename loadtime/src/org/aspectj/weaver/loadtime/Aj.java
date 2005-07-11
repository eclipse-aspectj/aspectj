/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.WeakHashMap;

import org.aspectj.weaver.tools.WeavingAdaptor;

/**
 * Adapter between the generic class pre processor interface and the AspectJ weaver
 * Load time weaving consistency relies on Bcel.setRepository
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Aj implements ClassPreProcessor {

    /**
     * Initialization
     */
    public void initialize() {
        ;
    }

    /**
     * Weave
     *
     * @param className
     * @param bytes
     * @param loader
     * @return
     */
    public byte[] preProcess(String className, byte[] bytes, ClassLoader loader) {
        //TODO AV needs to doc that
        if (loader == null || className == null) {
            // skip boot loader or null classes (hibernate)
            return bytes;
        }

        try {
            WeavingAdaptor weavingAdaptor = WeaverContainer.getWeaver(loader);
            byte[] weaved = weavingAdaptor.weaveClass(className, bytes);
            if (weavingAdaptor.shouldDump(className.replace('/', '.'))) {
                dump(className, weaved);
            }
            return weaved;
        } catch (Throwable t) {
            //FIXME AV wondering if we should have the option to fail (throw runtime exception) here
            // would make sense at least in test f.e. see TestHelper.handleMessage()
            t.printStackTrace();
            return bytes;
        }
    }

    /**
     * Cache of weaver
     * There is one weaver per classloader
     */
    static class WeaverContainer {

        private static Map weavingAdaptors = new WeakHashMap();

        static WeavingAdaptor getWeaver(ClassLoader loader) {
            synchronized(loader) {//FIXME AV - temp fix for #99861
                synchronized (weavingAdaptors) {
                    WeavingAdaptor weavingAdaptor = (WeavingAdaptor) weavingAdaptors.get(loader);
                    if (weavingAdaptor == null) {
                        weavingAdaptor = new ClassLoaderWeavingAdaptor(loader);
                        weavingAdaptors.put(loader, weavingAdaptor);
                    }
                    return weavingAdaptor;
                }
            }
        }
    }

    static void defineClass(ClassLoader loader, String name, byte[] bytes) {
        try {
            //TODO av protection domain, and optimize
            Method defineClass = ClassLoader.class.getDeclaredMethod(
                    "defineClass", new Class[]{
                        String.class, bytes.getClass(), int.class, int.class
                    }
            );
            defineClass.setAccessible(true);
            defineClass.invoke(
                    loader, new Object[]{
                        name,
                        bytes,
                        new Integer(0),
                        new Integer(bytes.length)
                    }
            );
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof LinkageError) {
                ;//is already defined (happens for X$ajcMightHaveAspect interfaces since aspects are reweaved)
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dump the given bytcode in _dump/... (dev mode)
     *
     * @param name
     * @param b
     * @throws Throwable
     */
    static void dump(String name, byte[] b) throws Throwable {
        String className = name.replace('.', '/');
        final File dir;
        if (className.indexOf('/') > 0) {
            dir = new File("_ajdump" + File.separator + className.substring(0, className.lastIndexOf('/')));
        } else {
            dir = new File("_ajdump");
        }
        dir.mkdirs();
        String fileName = "_ajdump" + File.separator + className + ".class";
        FileOutputStream os = new FileOutputStream(fileName);
        os.write(b);
        os.close();
    }

}
