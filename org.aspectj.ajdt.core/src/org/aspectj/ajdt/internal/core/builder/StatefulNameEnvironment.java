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

import java.util.Map;

import org.aspectj.weaver.bcel.UnwovenClassFile;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;


public class StatefulNameEnvironment implements INameEnvironment {
	Map classesFromName;
	INameEnvironment baseEnvironment;
	
	public StatefulNameEnvironment(INameEnvironment baseEnvironment, Map classesFromName) {
		this.classesFromName = classesFromName;
		this.baseEnvironment = baseEnvironment;
	}

	public void cleanup() {
		baseEnvironment.cleanup();
	}

	private NameEnvironmentAnswer findType(String name) {
		UnwovenClassFile cf = (UnwovenClassFile)classesFromName.get(name);
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
		//!!! need to use cache here too
		return baseEnvironment.isPackage(parentPackageName, packageName);

	}

}
