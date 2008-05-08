/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Matthew Webster, Adrian Colyer, 
 *     Martin Lippert     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.loadtime;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.aspectj.bridge.AbortException;
import org.aspectj.weaver.ExtensibleURLClassLoader;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;
import org.aspectj.weaver.tools.WeavingAdaptor;
import org.aspectj.weaver.tools.WeavingClassLoader;

public class WeavingURLClassLoader extends ExtensibleURLClassLoader implements WeavingClassLoader {

	public static final String WEAVING_CLASS_PATH = "aj.class.path"; 
	public static final String WEAVING_ASPECT_PATH = "aj.aspect.path"; 
	
	private URL[] aspectURLs;
	private WeavingAdaptor adaptor;
	private boolean initializingAdaptor;
	private Map generatedClasses = new HashMap(); /* String -> byte[] */ 
	
	private static Trace trace = TraceFactory.getTraceFactory().getTrace(WeavingURLClassLoader.class);

	/*
	 * This constructor is needed when using "-Djava.system.class.loader". 
	 */
	public WeavingURLClassLoader (ClassLoader parent) {
		this(getURLs(getClassPath()),getURLs(getAspectPath()),parent);
//		System.out.println("? WeavingURLClassLoader.WeavingURLClassLoader()");
	}
	
	public WeavingURLClassLoader (URL[] urls, ClassLoader parent) {
		super(urls,parent);
		if (trace.isTraceEnabled()) trace.enter("<init>",this,new Object[] { urls, parent });
//		System.out.println("WeavingURLClassLoader.WeavingURLClassLoader()");
		if (trace.isTraceEnabled()) trace.exit("<init>");
	}
	
	public WeavingURLClassLoader (URL[] classURLs, URL[] aspectURLs, ClassLoader parent) {
		super(classURLs,parent);
//		System.out.println("> WeavingURLClassLoader.WeavingURLClassLoader() classURLs=" + Arrays.asList(classURLs));
		this.aspectURLs = aspectURLs;
		
		/* If either we nor our parent is using an ASPECT_PATH use a new-style
		 * adaptor
		 */ 
		if (this.aspectURLs.length > 0 || getParent() instanceof WeavingClassLoader) {
			try {
				adaptor = new WeavingAdaptor(this);
			}
			catch (ExceptionInInitializerError ex) {
				ex.printStackTrace(System.out);
				throw ex;
			}
		}
//		System.out.println("< WeavingURLClassLoader.WeavingURLClassLoader() adaptor=" + adaptor);
	}
	
	private static String getAspectPath () {
		return System.getProperty(WEAVING_ASPECT_PATH,"");
	}
	
	private static String getClassPath () {
		return System.getProperty(WEAVING_CLASS_PATH,"");
	}
	
	private static URL[] getURLs (String path) {
		List urlList = new ArrayList();
		for (StringTokenizer t = new StringTokenizer(path,File.pathSeparator);
			 t.hasMoreTokens();) {
			File f = new File(t.nextToken().trim());
			try {
				if (f.exists()) {
					URL url = f.toURL();
					if (url != null) urlList.add(url);
				}
			} catch (MalformedURLException e) {}
		}

		URL[] urls = new URL[urlList.size()];
		urlList.toArray(urls);
		return urls;
	}

	protected void addURL(URL url) {
		adaptor.addURL(url);
		super.addURL(url);
	}

	/**
	 * Override to weave class using WeavingAdaptor 
	 */
	protected Class defineClass(String name, byte[] b, CodeSource cs) throws IOException {
		if (trace.isTraceEnabled()) trace.enter("defineClass",this,new Object[] { name, b, cs });
//		System.err.println("? WeavingURLClassLoader.defineClass(" + name + ", [" + b.length + "])");

		/* Avoid recursion during adaptor initialization */
		if (!initializingAdaptor) {
			
			/* Need to defer creation because of possible recursion during constructor execution */
			if (adaptor == null && !initializingAdaptor) {
				createAdaptor();
			}
			
			try {
				b = adaptor.weaveClass(name,b,false);
			}
			catch (AbortException ex) {
	    		trace.error("defineClass",ex);
	    		throw ex;
			}
			catch (Throwable th) {
	    		trace.error("defineClass",th);
			}
		}
		Class clazz = super.defineClass(name, b, cs);
		if (trace.isTraceEnabled()) trace.exit("defineClass",clazz);
		return clazz;
	}
	
	private void createAdaptor () {
		DefaultWeavingContext weavingContext = new DefaultWeavingContext (this) {

			/* Ensures consistent LTW messages for testing */
			public String getClassLoaderName() {
				return loader.getClass().getName();
			}
			
		};
		
		ClassLoaderWeavingAdaptor clwAdaptor = new ClassLoaderWeavingAdaptor();
		initializingAdaptor = true;
		clwAdaptor.initialize(this,weavingContext);
		initializingAdaptor = false;
		adaptor = clwAdaptor;
	}

	/**
	 * Override to find classes generated by WeavingAdaptor
	 */
	protected byte[] getBytes (String name) throws IOException {
		byte[] bytes = super.getBytes(name);
		
		if (bytes == null) {
//			return adaptor.findClass(name);
			return (byte[])generatedClasses.remove(name);
		}
		
		return bytes;
	}

	/**
	 * Implement method from WeavingClassLoader
	 */
	public URL[] getAspectURLs() {
		return aspectURLs;
	}

	public void acceptClass (String name, byte[] bytes) {
		generatedClasses.put(name,bytes);
	}

//	protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
//		System.err.println("> WeavingURLClassLoader.loadClass() name=" + name);
//		Class clazz= super.loadClass(name, resolve);
//		System.err.println("< WeavingURLClassLoader.loadClass() clazz=" + clazz + ", loader=" + clazz.getClassLoader());
//		return clazz;
//	}
	
//	private interface ClassPreProcessorAdaptor extends ClassPreProcessor {
//		public void addURL(URL url);
//	}
//	
//	private class WeavingAdaptorPreProcessor implements ClassPreProcessorAdaptor {
//		
//		private WeavingAdaptor adaptor;
//		
//		public WeavingAdaptorPreProcessor (WeavingClassLoader wcl) {
//			adaptor = new WeavingAdaptor(wcl);
//		}
//	    
//		public void initialize() {
//		}
//
//	    public byte[] preProcess(String className, byte[] bytes, ClassLoader classLoader) {
//	    	return adaptor.weaveClass(className,bytes);
//	    }
//
//		public void addURL(URL url) {
//			
//		}
//	}

}
