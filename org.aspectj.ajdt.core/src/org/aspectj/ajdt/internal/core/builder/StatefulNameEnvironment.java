/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

//import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aspectj.weaver.bcel.UnwovenClassFile;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;


public class StatefulNameEnvironment implements INameEnvironment {
	Map classesFromName;
	Set packageNames;
	INameEnvironment baseEnvironment;
	
	public StatefulNameEnvironment(INameEnvironment baseEnvironment, Map classesFromName) {
		this.classesFromName = classesFromName;
		this.baseEnvironment = baseEnvironment;
		
		packageNames = new HashSet();
		for (Iterator i = classesFromName.keySet().iterator(); i.hasNext(); ) {
			String className = (String)i.next();
			addAllPackageNames(className);
		}
//		System.err.println(packageNames);
	}

	private void addAllPackageNames(String className) {
		int dot = className.indexOf('.');
		while (dot != -1) {
			packageNames.add(className.substring(0, dot));
			dot = className.indexOf('.', dot+1);
		}
	}

	public void cleanup() {
		baseEnvironment.cleanup();
	}

	private NameEnvironmentAnswer findType(String name) {
		UnwovenClassFile cf = (UnwovenClassFile)classesFromName.get(name);
		//System.err.println("find: " + name + " found: " + cf);
		
		if (cf == null) return null;

		try {
			//System.out.println("from cache: " + name);
			return new NameEnvironmentAnswer(new ClassFileReader(cf.getBytes(), cf.getFilename().toCharArray()));
		} catch (ClassFormatException e) {
			return null; //!!! seems to match FileSystem behavior
		}
	}

	public NameEnvironmentAnswer findType(
		char[] typeName,
		char[][] packageName)
	{
		NameEnvironmentAnswer ret = findType(new String(CharOperation.concatWith(packageName, typeName, '.')));
		if (ret != null) return ret;
		return baseEnvironment.findType(typeName, packageName);
	}

	public NameEnvironmentAnswer findType(char[][] compoundName) {
		NameEnvironmentAnswer ret = findType(new String(CharOperation.concatWith(compoundName, '.')));
		if (ret != null) return ret;
		return baseEnvironment.findType(compoundName);
	}

	public boolean isPackage(char[][] parentPackageName, char[] packageName) {
		if (baseEnvironment.isPackage(parentPackageName, packageName)) return true;

		String fullPackageName = new String(CharOperation.concatWith(parentPackageName, packageName, '.'));
		return packageNames.contains(fullPackageName);
	}

}
