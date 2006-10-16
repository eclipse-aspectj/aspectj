/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

//import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.aspectj.util.FileUtil;


public class StatefulNameEnvironment implements INameEnvironment {
	private Map classesFromName;
	private Map inflatedClassFilesCache;
	private Set packageNames;
	private AjState state;
	private INameEnvironment baseEnvironment;
	
	public StatefulNameEnvironment(INameEnvironment baseEnvironment, Map classesFromName, AjState state) {
		this.classesFromName = classesFromName;
		this.inflatedClassFilesCache = new HashMap();
		this.baseEnvironment = baseEnvironment;
		this.state= state;
		
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
		this.classesFromName = Collections.EMPTY_MAP;
		this.packageNames = Collections.EMPTY_SET;
	}

	private NameEnvironmentAnswer findType(String name) {
	    // pr133532 - ask the state for the type first
		IBinaryType seenOnPreviousBuild = state.checkPreviousBuild(name);
		if (seenOnPreviousBuild!=null) {
			return new NameEnvironmentAnswer(seenOnPreviousBuild,null);
		}
		if (this.inflatedClassFilesCache.containsKey(name)) {
			return (NameEnvironmentAnswer) this.inflatedClassFilesCache.get(name);
		}
		else {
			File fileOnDisk = (File)classesFromName.get(name);
			//System.err.println("find: " + name + " found: " + cf);
			
			if (fileOnDisk == null) return null;
	
			try {
				//System.out.println("from cache: " + name);
				byte[] bytes = FileUtil.readAsByteArray(fileOnDisk);
				NameEnvironmentAnswer ret = 
					new NameEnvironmentAnswer(
						new ClassFileReader(bytes, fileOnDisk.getAbsolutePath().toCharArray()),
						null /* no access restriction */);
				this.inflatedClassFilesCache.put(name,ret);
				return ret;
			} catch (ClassFormatException e) {
				return null; //!!! seems to match FileSystem behavior
			} catch (IOException ex) {
				return null; // see above...
			}
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
