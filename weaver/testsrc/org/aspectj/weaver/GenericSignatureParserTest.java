/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
 package org.aspectj.weaver;

import junit.framework.TestCase;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.util.GenericSignatureParser;

/**
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class GenericSignatureParserTest extends TestCase {

	GenericSignatureParser parser;

	protected void setUp() throws Exception {
		super.setUp();
		parser = new GenericSignatureParser();
	}

	public void testClassSignatureParsingInJDK() throws Exception {
		SyntheticRepository repository = SyntheticRepository.getInstance();
		String[] testClasses = new String[] { "java.lang.Comparable", "java.lang.Iterable", "java.lang.Class", "java.lang.Enum",
				"java.lang.InheritableThreadLocal", "java.lang.ThreadLocal", "java.util.Collection", "java.util.Comparator",
				"java.util.Enumeration", "java.util.Iterator", "java.util.List", "java.util.ListIterator", "java.util.Map",
				"java.util.Map$Entry", "java.util.Queue", "java.util.Set", "java.util.SortedMap", "java.util.SortedSet" };
		for (int i = 0; i < testClasses.length; i++) {
			JavaClass jc = repository.loadClass(testClasses[i]);
			String sig = jc.getGenericSignature();
			parser.parseAsClassSignature(sig);
		}
	}

	public void testMethodSignatureParsingInJDK() throws Exception {
		SyntheticRepository repository = SyntheticRepository.getInstance();
		String[] testClasses = new String[] { "java.lang.Comparable", "java.lang.Iterable", "java.lang.Class", "java.lang.Enum",
				"java.lang.InheritableThreadLocal", "java.lang.ThreadLocal", "java.util.Collection", "java.util.Comparator",
				"java.util.Enumeration", "java.util.Iterator", "java.util.List", "java.util.ListIterator", "java.util.Map",
				"java.util.Map$Entry", "java.util.Queue", "java.util.Set", "java.util.SortedMap", "java.util.SortedSet" };
		for (int i = 0; i < testClasses.length; i++) {
			JavaClass jc = repository.loadClass(testClasses[i]);
			Method[] methods = jc.getMethods();
			for (int j = 0; j < methods.length; j++) {
				String sig = methods[j].getGenericSignature();
				if (sig != null)
					parser.parseAsMethodSignature(sig);
			}
		}
	}

}
