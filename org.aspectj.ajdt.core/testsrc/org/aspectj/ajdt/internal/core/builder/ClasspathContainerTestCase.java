/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.io.IOException;

import junit.framework.TestCase;

import org.aspectj.util.FileUtil;
import org.aspectj.workbench.resources.FilesystemFolder;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;

public class ClasspathContainerTestCase extends TestCase {

	public ClasspathContainerTestCase(String name) {
		super(name);
	}

	// XXX add some inner cases
	public void testFindClass() throws IOException {
		FileUtil.extractJar("testdata/testclasses.jar", "out/testclasses");
		
		
		IContainer container = new FilesystemFolder(new Path("out/testclasses"));
		ClasspathLocation classpathLocation = new ClasspathContainer(container);
		// put back in for sanity check
		//classpathLocation = ClasspathContainer.forBinaryFolder("testdata/testclasses");
		
		NameEnvironmentAnswer answer = classpathLocation.findClass("Hello.class", "", "Hello.class");
		assertTrue("" + answer, answer != null);
		
		NameEnvironmentAnswer answer2 = classpathLocation.findClass("Foo.class", "p1", "p1/Foo.class");
		assertTrue("" + answer2, answer2 != null);
		
		NameEnvironmentAnswer answer3 = classpathLocation.findClass("DoesNotExist.class", "", "DoesNotExist.class");
		assertTrue("" + answer3, answer3 == null);
		
		NameEnvironmentAnswer answer4 = classpathLocation.findClass("DoesNotExist.class", "p1", "DoesNotExist.class");
		assertTrue("" + answer4, answer4 == null);
		
		
	}

	public void testIsPackage() {
		IContainer container = new FilesystemFolder(new Path("testdata/testclasses"));
		ClasspathLocation classpathLocation = new ClasspathContainer(container);
		
		assertTrue("is a package", classpathLocation.isPackage("p1"));
		assertTrue("is not a package", !classpathLocation.isPackage("mumble"));
		assertTrue("is not a package", !classpathLocation.isPackage("Hello.class"));
	}

}
