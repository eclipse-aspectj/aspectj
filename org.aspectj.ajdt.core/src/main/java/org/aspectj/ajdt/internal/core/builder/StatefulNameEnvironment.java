/* *******************************************************************
 * Copyright (c) 2002 IBM and other contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Palo Alto Research Center, Incorporated (PARC)
 *     Andy Clement
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IModule;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.aspectj.util.FileUtil;

public class StatefulNameEnvironment implements IModuleAwareNameEnvironment {
	private Map<String,File> classesFromName;
	private Map<String,NameEnvironmentAnswer> inflatedClassFilesCache;
	private Set<String> packageNames;
	private AjState state;
	private IModuleAwareNameEnvironment baseEnvironment;

	public StatefulNameEnvironment(IModuleAwareNameEnvironment baseEnvironment, Map<String,File> classesFromName, AjState state) {
		this.classesFromName = classesFromName;
		this.inflatedClassFilesCache = new HashMap<>();
		this.baseEnvironment = baseEnvironment;
		this.state = state;
		packageNames = new HashSet<>();
		for (String className: classesFromName.keySet()) {
			addAllPackageNames(className);
		}
	}

	private void addAllPackageNames(String className) {
		int dot = className.indexOf('.');
		while (dot != -1) {
			packageNames.add(className.substring(0, dot));
			dot = className.indexOf('.', dot + 1);
		}
	}

	private NameEnvironmentAnswer findType(String name) {
		// pr133532 - ask the state for the type first
		IBinaryType seenOnPreviousBuild = state.checkPreviousBuild(name);
		if (seenOnPreviousBuild != null) {
			return new NameEnvironmentAnswer(seenOnPreviousBuild, null);
		}
		if (this.inflatedClassFilesCache.containsKey(name)) {
			return this.inflatedClassFilesCache.get(name);
		} else {
			File fileOnDisk = classesFromName.get(name);
			// System.err.println("find: " + name + " found: " + cf);
			if (fileOnDisk == null) {
				return null;
			}
			try {
				// System.out.println("from cache: " + name);
				byte[] bytes = FileUtil.readAsByteArray(fileOnDisk);
				NameEnvironmentAnswer ret = new NameEnvironmentAnswer(new ClassFileReader(bytes, fileOnDisk.getAbsolutePath()
						.toCharArray()), null /* no access restriction */);
				this.inflatedClassFilesCache.put(name, ret);
				return ret;
			} catch (ClassFormatException e) {
				return null; // !!! seems to match FileSystem behavior
			} catch (IOException ex) {
				return null; // see above...
			}
		}
	}

	@Override
	public void cleanup() {
		baseEnvironment.cleanup();
		this.classesFromName = Collections.emptyMap();
		this.packageNames.clear();
	}

	@Override
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
		NameEnvironmentAnswer ret = findType(new String(CharOperation.concatWith(packageName, typeName, '.')));
		if (ret != null) {
			return ret;
		}
		return baseEnvironment.findType(typeName, packageName);
	}

	@Override
	public NameEnvironmentAnswer findType(char[][] compoundName) {
		NameEnvironmentAnswer ret = findType(new String(CharOperation.concatWith(compoundName, '.')));
		if (ret != null) {
			return ret;
		}
		return baseEnvironment.findType(compoundName);
	}

	@Override
	public boolean isPackage(char[][] parentPackageName, char[] packageName) {
		if (baseEnvironment.isPackage(parentPackageName, packageName)) {
			return true;
		}
		String fullPackageName = new String(CharOperation.concatWith(parentPackageName, packageName, '.'));
		return packageNames.contains(fullPackageName);
	}

	/**
	 * Needs to be told about changes. The 'added' set is a subset of classNameToFileMap consisting of just those names added during
	 * this build - to reduce any impact on incremental compilation times.
	 */
	public void update(Map<String,File> classNameToFileMap, Set<String> added) {
		for (String className: added) {
			addAllPackageNames(className);
		}
		this.classesFromName = classNameToFileMap;
	}

	@Override
	public NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
		return baseEnvironment.findType(compoundName, moduleName);
	}

	@Override
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
		return baseEnvironment.findType(typeName, packageName, moduleName);
	}


	@Override
	public boolean hasCompilationUnit(char[][] qualifiedPackageName, char[] moduleName, boolean checkCUs) {
		return baseEnvironment.hasCompilationUnit(qualifiedPackageName, moduleName, checkCUs);
	}

	@Override
	public IModule getModule(char[] moduleName) {
		return baseEnvironment.getModule(moduleName);
	}

	@Override
	public char[][] getAllAutomaticModules() {
		return baseEnvironment.getAllAutomaticModules();
	}

	@Override
	public char[][] getModulesDeclaringPackage(char[][] arg0, char[] arg1) {
		return baseEnvironment.getModulesDeclaringPackage(arg0, arg1);
	}

	@Override
	public char[][] listPackages(char[] arg0) {
		return baseEnvironment.listPackages(arg0);
	}

}
