/*******************************************************************************
 * Copyright (c) 2006,2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.testing.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author Matthew Webster
 * @author Andy Clement
 */
public class TestServer implements Runnable {

	private static final boolean debug = Boolean.getBoolean("org.aspectj.testing.server.debug");

	private boolean exitOnError = true;
	private File workingDirectory;
	private ClassLoader rootLoader;
	private Map<String,ClassLoader> loaders = new HashMap<>();

	private String mainClass = "UnknownClass";
	private String mainLoader = "UnknownLoader";

	public void initialize () throws IOException {
		createRootLoader();
		loadConfiguration();
	}

	private void loadConfiguration () throws IOException {
		File file = new File(workingDirectory,"server.properties");
		Properties props = new Properties();
		FileInputStream in = new FileInputStream(file);
		props.load(in);
		in.close();

		Enumeration<?> enu = props.propertyNames();
		while (enu.hasMoreElements()) {
			String key = (String)enu.nextElement();
			if (key.startsWith("loader.")) {
				createLoader(props.getProperty(key));
			}
			else if (key.equals("main")) {
				StringTokenizer st = new StringTokenizer(props.getProperty(key),",");
				mainClass = st.nextToken();
				mainLoader = st.nextToken();
			}
		}
	}

	private void createLoader (String property) throws IOException {
		ClassLoader parent = rootLoader;

		StringTokenizer st = new StringTokenizer(property,",");
		String name = st.nextToken();
		String classpath = st.nextToken();
		if (debug) System.err.println("Creating loader "+name+" with classpath "+classpath);
		if (st.hasMoreTokens()) {
			String parentName = st.nextToken();
			parent = loaders.get(parentName);
			if (parent == null) error("No such loader: " + parentName);
		}

		List<URL> urlList = new ArrayList<>();
		st = new StringTokenizer(classpath,";");
		while (st.hasMoreTokens()) {
			String fileName = st.nextToken();
			File file = new File(workingDirectory,fileName).getCanonicalFile();
			if (!file.exists()) error("Missing or invalid file: " + file.getPath());
			URL url = file.toURI().toURL();
			urlList.add(url);
		}
		URL[] urls = new URL[urlList.size()];
		urlList.toArray(urls);
		ClassLoader loader = new URLClassLoader(urls, parent);
		if (debug) System.err.println("? TestServer.createLoader() loader=" + loader + ", name='" + name + "', urls=" + urlList + ", parent=" + parent);

		loaders.put(name,loader);
	}

	private void createRootLoader() throws IOException {
		List<URL> urlList = new ArrayList<>();

		// Sandbox
		URL url = workingDirectory.getCanonicalFile().toURI().toURL();
		urlList.add(url);

		// Find the AspectJ root folder (i.e. org.aspectj)
		File aspectjBase = new File(".").getCanonicalFile();
		while (aspectjBase!= null && !aspectjBase.getName().equals("org.aspectj")) {
			aspectjBase = aspectjBase.getParentFile();
		}
		if (aspectjBase == null) {
			error("Unable to locate 'org.aspectj' in "+new File(".").getCanonicalPath());
		}
		urlList.add(new File(aspectjBase,"runtime/target/classes").toURI().toURL());
//		urlList.add(new File(aspectjBase,"aspectjrt/target/classes").toURI().toURL());
//		urlList.add(new File(aspectjBase,"aspectj5rt/target/classes").toURI().toURL());

		URL[] urls = new URL[urlList.size()];
		urlList.toArray(urls);
		ClassLoader parent = getClass().getClassLoader().getParent();
		rootLoader = new URLClassLoader(urls,parent);
		if (debug) System.err.println("? TestServer.createRootLoader() loader=" + rootLoader + ", urlList=" + urlList + ", parent=" + parent);
	}

	public void setExitOntError (boolean b) {
		exitOnError = b;
	}

	public void setWorkingDirectory (String name) {
		workingDirectory = new File(name);
		if (!workingDirectory.exists()) error("Missing or invalid working directory: " + workingDirectory.getPath());
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Starting ...");

		TestServer server = new TestServer();
		server.setWorkingDirectory(args[0]);
		server.initialize();

		Thread thread = new Thread(server,"application");
		thread.start();
		thread.join();

		System.out.println("Stopping ...");
	}

	public void run() {
		System.out.println("Running " + mainClass);
		runClass(mainClass,loaders.get(mainLoader));
	}

	private void runClass (String className, ClassLoader classLoader) {
		try {
			Class<?> clazz = Class.forName(className,false,classLoader);
			invokeMain(clazz,new String[] {});
		}
		catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			error(ex.toString());
		}
	}

	public void invokeMain (Class<?> clazz, String[] args)
	{
		Class<?>[] paramTypes = new Class[1];
		paramTypes[0] = args.getClass();

		try {
			Method method = clazz.getDeclaredMethod("main",paramTypes);
			Object[] params = new Object[1];
			params[0] = args;
			method.invoke(null,params);
		}
		catch (InvocationTargetException ex) {
			Throwable th = ex.getTargetException();
			th.printStackTrace();
			error(th.toString());
		}
		catch (Throwable th) {
			th.printStackTrace();
			error(th.toString());
		}
	}

	private void error (String message) {
		System.out.println(message);
		if (exitOnError) System.exit(0);
	}
}
