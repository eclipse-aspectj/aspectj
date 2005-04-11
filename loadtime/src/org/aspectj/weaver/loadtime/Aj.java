/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.ICrossReferenceHandler;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.bcel.BcelWeaver;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;
import org.aspectj.weaver.tools.GeneratedClassHandler;
import org.aspectj.weaver.tools.WeavingAdaptor;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
        //System.out.println("Aj.preProcess " + className + " @ " + loader + " " + Thread.currentThread());
        //TODO av needs to spec and doc that
        //TODO av should skip org.aspectj as well unless done somewhere else
        if (loader == null
                || className == null) {
            // skip boot loader or null classes (hibernate)
            return bytes;
        }

        try {
            byte[] weaved = WeaverContainer.getWeaver(loader).weaveClass(className, bytes);
            //TODO av make dump optionnal and configurable
            __dump(className, weaved);
            return weaved;
        } catch (Throwable t) {
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
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Dump the given bytcode in _dump/...
     *
     * @param name
     * @param b
     * @throws Throwable
     */
    static void __dump(String name, byte[] b) throws Throwable {
        String className = name.replace('.', '/');
        final File dir;
        if (className.indexOf('/') > 0) {
            dir = new File("_dump" + File.separator + className.substring(0, className.lastIndexOf('/')));
        } else {
            dir = new File("_dump");
        }
        dir.mkdirs();
        String fileName = "_dump" + File.separator + className + ".class";
        FileOutputStream os = new FileOutputStream(fileName);
        os.write(b);
        os.close();
    }

}
