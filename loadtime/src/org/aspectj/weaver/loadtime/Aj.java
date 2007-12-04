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

import java.util.Map;
import java.util.WeakHashMap;

import org.aspectj.weaver.Dump;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;
import org.aspectj.weaver.tools.WeavingAdaptor;

/**
 * Adapter between the generic class pre processor interface and the AspectJ weaver
 * Load time weaving consistency relies on Bcel.setRepository
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Aj implements ClassPreProcessor {

	private IWeavingContext weavingContext;
	
	private static Trace trace = TraceFactory.getTraceFactory().getTrace(Aj.class);
	
	public Aj(){
		this(null);
	}
	
	
	public Aj(IWeavingContext context){
		if (trace.isTraceEnabled()) trace.enter("<init>",this,new Object[] {context, getClass().getClassLoader()});
		this.weavingContext = context;
		if (trace.isTraceEnabled()) trace.exit("<init>");
	}

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
     * @return weaved bytes
     */
    public byte[] preProcess(String className, byte[] bytes, ClassLoader loader) {
    	
        //TODO AV needs to doc that
        if (loader == null || className == null) {
            // skip boot loader or null classes (hibernate)
            return bytes;
        }

        if (trace.isTraceEnabled()) trace.enter("preProcess",this,new Object[] {className, bytes, loader});
        if (trace.isTraceEnabled()) trace.event("preProcess",this,new Object[] {loader.getParent(), Thread.currentThread().getContextClassLoader()});

        try {
        	synchronized (loader) {
                WeavingAdaptor weavingAdaptor = WeaverContainer.getWeaver(loader, weavingContext);
                if (weavingAdaptor == null) {
            		if (trace.isTraceEnabled()) trace.exit("preProcess");
                	return bytes;
                }
                byte[] newBytes = weavingAdaptor.weaveClass(className, bytes,false);
                Dump.dumpOnExit(weavingAdaptor.getMessageHolder(), true);
        		if (trace.isTraceEnabled()) trace.exit("preProcess",newBytes);
                return newBytes;
			}
        
        /* Don't like to do this but JVMTI swallows all exceptions */
        } catch (Throwable th) {
    		trace.error(className,th);
    		Dump.dumpWithException(th);
            //FIXME AV wondering if we should have the option to fail (throw runtime exception) here
            // would make sense at least in test f.e. see TestHelper.handleMessage()
    		if (trace.isTraceEnabled()) trace.exit("preProcess",th);
            return bytes;
        }
    }

    /**
     * Cache of weaver
     * There is one weaver per classloader
     */
    static class WeaverContainer {

        private final static Map weavingAdaptors = new WeakHashMap();

        static WeavingAdaptor getWeaver(ClassLoader loader, IWeavingContext weavingContext) {
            ExplicitlyInitializedClassLoaderWeavingAdaptor adaptor = null;
            synchronized(weavingAdaptors) {
                adaptor = (ExplicitlyInitializedClassLoaderWeavingAdaptor) weavingAdaptors.get(loader);
                if (adaptor == null) {
                	String loaderClassName = loader.getClass().getName(); 
                	if (loaderClassName.equals("sun.reflect.DelegatingClassLoader")) {
                		// we don't weave reflection generated types at all! 
                		return null;
                	} else {
	                    // create it and put it back in the weavingAdaptors map but avoid any kind of instantiation
	                    // within the synchronized block
	                    ClassLoaderWeavingAdaptor weavingAdaptor = new ClassLoaderWeavingAdaptor();
	                    adaptor = new ExplicitlyInitializedClassLoaderWeavingAdaptor(weavingAdaptor);
	                    weavingAdaptors.put(loader, adaptor);
                	}
                }
            }
            // perform the initialization
            return adaptor.getWeavingAdaptor(loader, weavingContext);


            // old version
//            synchronized(loader) {//FIXME AV - temp fix for #99861
//                synchronized (weavingAdaptors) {
//                    WeavingAdaptor weavingAdaptor = (WeavingAdaptor) weavingAdaptors.get(loader);
//                    if (weavingAdaptor == null) {
//                        weavingAdaptor = new ClassLoaderWeavingAdaptor(loader, weavingContext);
//                        weavingAdaptors.put(loader, weavingAdaptor);
//                    }
//                    return weavingAdaptor;
//                }
//            }
        }
    }

    static class ExplicitlyInitializedClassLoaderWeavingAdaptor {
        private final ClassLoaderWeavingAdaptor weavingAdaptor;
        private boolean isInitialized;

        public ExplicitlyInitializedClassLoaderWeavingAdaptor(ClassLoaderWeavingAdaptor weavingAdaptor) {
            this.weavingAdaptor = weavingAdaptor;
            this.isInitialized = false;
        }

        private void initialize(ClassLoader loader, IWeavingContext weavingContext) {
            if (!isInitialized) {
                isInitialized = true;
                weavingAdaptor.initialize(loader, weavingContext);
            }
        }

        public ClassLoaderWeavingAdaptor getWeavingAdaptor(ClassLoader loader, IWeavingContext weavingContext) {
            initialize(loader, weavingContext);
            return weavingAdaptor;
        }
    }

    /**
     * Returns a namespace based on the contest of the aspects available
     */
    public String getNamespace (ClassLoader loader) {
        ClassLoaderWeavingAdaptor weavingAdaptor = (ClassLoaderWeavingAdaptor)WeaverContainer.getWeaver(loader, weavingContext);
    	return weavingAdaptor.getNamespace();
    }
    
    /**
     * Check to see if any classes have been generated for a particular classes loader.
     * Calls ClassLoaderWeavingAdaptor.generatedClassesExist()
     * @param loader the class cloder
     * @return       true if classes have been generated.
     */
    public boolean generatedClassesExist(ClassLoader loader){
    	return ((ClassLoaderWeavingAdaptor)WeaverContainer.getWeaver(loader, weavingContext)).generatedClassesExistFor(null);
    }
    
    public void flushGeneratedClasses(ClassLoader loader){
    	((ClassLoaderWeavingAdaptor)WeaverContainer.getWeaver(loader, weavingContext)).flushGeneratedClasses();
    }

}