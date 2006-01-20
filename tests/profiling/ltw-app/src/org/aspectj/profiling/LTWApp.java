/* *******************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.profiling;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Application that takes a single argument containing the name of a
 * jar file, and performs Class.forName on every class within it.
 */
public class LTWApp {
	
	private File inJar;
	private int numLoaded = 0;
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expecting a single jar file argument");
		}
		new LTWApp(args[0]).run();
	}
	
	public LTWApp(String jarFileName) {
		inJar = new File(jarFileName);
		if (!inJar.exists() || !inJar.canRead()) {
			throw new IllegalArgumentException("File '" + jarFileName +
					"' does not exist or cannot be read");
		}
	}
	
	public void run() throws IOException {
		ZipInputStream inStream = new ZipInputStream(new FileInputStream(inJar));
		long startTime = System.currentTimeMillis();
		while (true) {
			ZipEntry entry = inStream.getNextEntry();
			if (entry == null) break;
			
			if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
				continue;
			}
			
			loadClass(entry.getName());
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Loaded " + numLoaded + " classes in " + (endTime - startTime) + " milliseconds");
	}
	
	private void loadClass(String classFileName) {
		String className = classFileName.substring(0,(classFileName.length() - ".class".length()));
		className = className.replace('/','.');
		try {
			Class c = Class.forName(className);
		} 
		catch(ClassNotFoundException ex) {
			throw new IllegalStateException("Unable to load class defined in input jar file, check that jar is also on the classpath!");
		}
		numLoaded++;
	}
}