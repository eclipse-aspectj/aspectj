/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import java.util.Arrays;

import org.aspectj.apache.bcel.Repository;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.util.GenericSignature;
import org.aspectj.util.GenericSignature.ClassSignature;
import org.aspectj.util.GenericSignatureParser;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.UnresolvedType;

import junit.framework.TestCase;

/**
 * @author colyer
 * 
 */
public class BcelGenericSignatureToTypeXTestCase extends TestCase {

	public final GenericSignature.ClassSignature getGenericClassTypeSignature(JavaClass jClass) {
		Signature sig = jClass.getSignatureAttribute();
		if (sig != null) {
			GenericSignatureParser parser = new GenericSignatureParser();
			ClassSignature classSig = parser.parseAsClassSignature(sig.getSignature());
			return classSig;
		}
		return null;
	}

	public void testEnumFromHell() throws Exception {
		BcelWorld world = new BcelWorld();
		JavaClass javaLangEnum = Repository.lookupClass("java/lang/Enum");
		GenericSignature.ClassSignature cSig = getGenericClassTypeSignature(javaLangEnum);
		UnresolvedType superclass = BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(cSig.superclassSignature,
				cSig.formalTypeParameters, world);
		assertEquals("Ljava/lang/Object;", superclass.getSignature());
		System.out.println(Arrays.toString(cSig.superInterfaceSignatures));
		if (LangUtil.is12VMOrGreater()) {
			// [Ljava/lang/constant/Constable;, Ljava/lang/Comparable<TE;>;, Ljava/io/Serializable;]
			assertEquals("3 superinterfaces but "+Arrays.toString(cSig.superInterfaceSignatures), 3, cSig.superInterfaceSignatures.length);
		} else {
			assertEquals("2 superinterfaces", 2, cSig.superInterfaceSignatures.length);
		}
		int idx = LangUtil.is12VMOrGreater()?1:0;
		UnresolvedType comparable = BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(cSig.superInterfaceSignatures[idx++],
				cSig.formalTypeParameters, world);
		assertEquals("Pjava/lang/Comparable<TE;>;", comparable.getSignature());
		UnresolvedType serializable = BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
				cSig.superInterfaceSignatures[idx++], cSig.formalTypeParameters, world);
		assertEquals("Ljava/io/Serializable;", serializable.getSignature());
	}

	public void testColonColon() throws Exception {
		BcelWorld world = new BcelWorld();
		GenericSignature.ClassSignature cSig = new GenericSignatureParser()
				.parseAsClassSignature("<T::Ljava/io/Serializable;>Ljava/lang/Object;Ljava/lang/Comparable<TT;>;");
		UnresolvedType resolved = BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(cSig.superclassSignature,
				cSig.formalTypeParameters, world);
		assertEquals("Ljava/lang/Object;", resolved.getSignature());
		BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(cSig.superInterfaceSignatures[0], cSig.formalTypeParameters,
				world);

	}

}
