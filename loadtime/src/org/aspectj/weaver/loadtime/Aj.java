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

    /**
     * Adaptor with the AspectJ WeavingAdaptor
     */
    static class ClassLoaderWeavingAdaptor extends WeavingAdaptor {

        public ClassLoaderWeavingAdaptor(final ClassLoader loader) {
            super(null);// at this stage we don't have yet a generatedClassHandler to define to the VM the closures
            this.generatedClassHandler = new GeneratedClassHandler() {
                /**
                 * Callback when we need to define a Closure in the JVM
                 *
                 * @param name
                 * @param bytes
                 */
                public void acceptClass(String name, byte[] bytes) {
                    //TODO av make dump configurable
                    try {
                        __dump(name, bytes);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    defineClass(loader, name, bytes);// could be done lazily using the hook
                }
            };

            bcelWorld = new BcelWorld(
                    loader, messageHandler, new ICrossReferenceHandler() {
                        public void addCrossReference(ISourceLocation from, ISourceLocation to, IRelationship.Kind kind, boolean runtimeTest) {
                            ;// for tools only
                        }
                    }
            );

//            //TODO this AJ code will call
//            //org.aspectj.apache.bcel.Repository.setRepository(this);
//            //ie set some static things
//            //==> bogus as Bcel is expected to be
//            org.aspectj.apache.bcel.Repository.setRepository(new ClassLoaderRepository(loader));

            weaver = new BcelWeaver(bcelWorld);

            // register the definitions
            registerDefinitions(weaver, loader);

            // after adding aspects
            weaver.prepareForWeave();
        }
    }

    private static void defineClass(ClassLoader loader, String name, byte[] bytes) {
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
    private static void __dump(String name, byte[] b) throws Throwable {
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

    /**
     * Load and cache the aop.xml/properties according to the classloader visibility rules
     *
     * @param weaver
     * @param loader
     */
    private static void registerDefinitions(final BcelWeaver weaver, final ClassLoader loader) {
        try {
            //TODO av underoptimized: we will parse each XML once per CL that see it
            Enumeration xmls = loader.getResources("/META-INF/aop.xml");
            List definitions = new ArrayList();

            //TODO av dev mode needed ? TBD -Daj5.def=...
            if (loader != null && loader != ClassLoader.getSystemClassLoader().getParent()) {
                String file = System.getProperty("aj5.def", null);
                if (file != null) {
                    definitions.add(DocumentParser.parse((new File(file)).toURL()));
                }
            }

            while (xmls.hasMoreElements()) {
                URL xml = (URL) xmls.nextElement();
                definitions.add(DocumentParser.parse(xml));
            }
            registerOptions(weaver, loader, definitions);
            registerAspects(weaver, loader, definitions);
            registerFilters(weaver, loader, definitions);
        } catch (Exception e) {
            weaver.getWorld().getMessageHandler().handleMessage(
                    new Message("Register definition failed", IMessage.FAIL, e, null)
            );
        }
    }

    /**
     * Configure the weaver according to the option directives
     * TODO av - don't know if it is that good to reuse, since we only allow a small subset of options in LTW
     *
     * @param weaver
     * @param loader
     * @param definitions
     */
    private static void registerOptions(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        StringBuffer allOptions = new StringBuffer();
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            allOptions.append(definition.getWeaverOptions()).append(' ');
        }

        Options.WeaverOption weaverOption = Options.parse(allOptions.toString(), loader);

        // configure the weaver and world
        // AV - code duplicates AspectJBuilder.initWorldAndWeaver()
        World world = weaver.getWorld();
        world.setMessageHandler(weaverOption.messageHandler);
        world.setXlazyTjp(weaverOption.lazyTjp);
        weaver.setReweavableMode(weaverOption.reWeavable, false);
        world.setXnoInline(weaverOption.noInline);
        world.setBehaveInJava5Way(weaverOption.java5);
        //TODO proceedOnError option
    }

    /**
     * Register the aspect, following include / exclude rules
     *
     * @param weaver
     * @param loader
     * @param definitions
     */
    private static void registerAspects(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        //TODO: the exclude aspect allow to exclude aspect defined upper in the CL hierarchy - is it what we want ??
        // if not, review the getResource so that we track which resource is defined by which CL

        //it aspectClassNames
        //exclude if in any of the exclude list
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            Definition definition = (Definition) iterator.next();
            for (Iterator aspects = definition.getAspectClassNames().iterator(); aspects.hasNext();) {
                String aspectClassName = (String) aspects.next();
                if (!Definition.isAspectExcluded(aspectClassName, definitions)) {
                    weaver.addLibraryAspect(aspectClassName);
                }
            }
        }

        //it concreteAspects
        //exclude if in any of the exclude list
        //TODO
    }

    /**
     * Register the include / exclude filters
     *
     * @param weaver
     * @param loader
     * @param definitions
     */
    private static void registerFilters(final BcelWeaver weaver, final ClassLoader loader, final List definitions) {
        //TODO
        ;
    }
}
