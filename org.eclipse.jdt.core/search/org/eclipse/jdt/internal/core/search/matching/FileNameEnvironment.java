/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.core.JavaModelManager;

public class FileNameEnvironment extends FileSystem {
	
public FileNameEnvironment(String[] classpathNames, String encoding, int[] classpathDirectoryModes) {
	super(classpathNames, new String[0], encoding, classpathDirectoryModes);
}

public ClasspathJar getClasspathJar(File file) throws IOException {
	try {
		ZipFile zipFile = JavaModelManager.getJavaModelManager().getZipFile(new Path(file.getPath()));
		return new ClasspathJar(zipFile, false);
	} catch (CoreException e) {
		return super.getClasspathJar(file);
	}
}
}
