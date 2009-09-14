/* *******************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation {date}
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePairGen;
import org.aspectj.apache.bcel.classfile.annotation.ElementValueGen;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValueGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;

/**
 * Super class for the Java5 tests, includes various helper methods.
 */

public class BcelTestCase extends TestCase {

	private boolean verbose = false;

	protected File createTestdataFile(String name) {
		return new File("testdata" + File.separator + name);
	}

	protected JavaClass getClassFromJar(String clazzname) throws ClassNotFoundException {
		SyntheticRepository repos = createRepos("testcode.jar");
		return repos.loadClass(clazzname);
	}

	protected Method getMethod(JavaClass cl, String methodname) {
		Method[] methods = cl.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (m.getName().equals(methodname)) {
				return m;
			}
		}
		return null;
	}

	protected boolean wipe(String name) {
		return new File("testdata" + File.separator + name).delete();
	}

	protected boolean wipe(String dir, String name) {
		boolean b = wipe(dir + File.separator + name);
		String[] files = new File(dir).list();
		if (files == null || files.length == 0) {
			new File(dir).delete(); // Why does this not succeed? stupid thing
		}
		return b;
	}

	public SyntheticRepository createRepos(String cpentry) {
		ClassPath cp = new ClassPath("testdata" + File.separator + cpentry + File.pathSeparator
				+ System.getProperty("java.class.path"));
		return SyntheticRepository.getInstance(cp);
	}

	protected Attribute[] findAttribute(String name, JavaClass clazz) {
		Attribute[] all = clazz.getAttributes();
		List<Attribute> chosenAttrsList = new ArrayList<Attribute>();
		for (int i = 0; i < all.length; i++) {
			if (verbose)
				System.err.println("Attribute: " + all[i].getName());
			if (all[i].getName().equals(name))
				chosenAttrsList.add(all[i]);
		}
		return chosenAttrsList.toArray(new Attribute[] {});
	}

	protected Attribute findAttribute(String name, Attribute[] all) {
		List<Attribute> chosenAttrsList = new ArrayList<Attribute>();
		for (int i = 0; i < all.length; i++) {
			if (verbose)
				System.err.println("Attribute: " + all[i].getName());
			if (all[i].getName().equals(name))
				chosenAttrsList.add(all[i]);
		}
		assertTrue("Should be one match: " + chosenAttrsList.size(), chosenAttrsList.size() == 1);
		return chosenAttrsList.get(0);
	}

	protected String dumpAnnotations(AnnotationGen[] as) {
		StringBuffer result = new StringBuffer();
		result.append("[");
		for (int i = 0; i < as.length; i++) {
			AnnotationGen annotation = as[i];
			result.append(annotation.toShortString());
			if (i + 1 < as.length)
				result.append(",");
		}
		result.append("]");
		return result.toString();
	}

	protected String dumpAnnotations(List<AnnotationGen> as) {
		StringBuffer result = new StringBuffer();
		result.append("[");
		for (int i = 0; i < as.size(); i++) {
			AnnotationGen annotation = as.get(i);
			result.append(annotation.toShortString());
			if (i + 1 < as.size())
				result.append(",");
		}
		result.append("]");
		return result.toString();
	}

	protected String dumpAttributes(Attribute[] as) {
		StringBuffer result = new StringBuffer();
		result.append("AttributeArray:[");
		for (int i = 0; i < as.length; i++) {
			Attribute attr = as[i];
			result.append(attr.toString());
			if (i + 1 < as.length)
				result.append(",");
		}
		result.append("]");
		return result.toString();
	}

	public AnnotationGen createFruitAnnotation(ConstantPool cp, String aFruit, boolean visibility) {
		SimpleElementValueGen evg = new SimpleElementValueGen(ElementValueGen.STRING, cp, aFruit);
		ElementNameValuePairGen nvGen = new ElementNameValuePairGen("fruit", evg, cp);
		ObjectType t = new ObjectType("SimpleStringAnnotation");
		List<ElementNameValuePairGen> elements = new ArrayList<ElementNameValuePairGen>();
		elements.add(nvGen);
		return new AnnotationGen(t, elements, visibility, cp);
	}

}
