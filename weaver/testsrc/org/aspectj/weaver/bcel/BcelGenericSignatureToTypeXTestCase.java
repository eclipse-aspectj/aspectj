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

import org.aspectj.apache.bcel.Repository;
import org.aspectj.apache.bcel.classfile.GenericSignatureParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 */
public class BcelGenericSignatureToTypeXTestCase extends TestCase {

	public void testEnumFromHell() {
		BcelWorld world = new BcelWorld();
		JavaClass javaLangEnum = Repository.lookupClass("java/lang/Enum");
		Signature.ClassSignature cSig = javaLangEnum.getGenericClassTypeSignature();
		UnresolvedType superclass = 
			BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
					cSig.superclassSignature,
					cSig.formalTypeParameters,
					world
					);
		assertEquals("Ljava/lang/Object;",superclass.getSignature());
		assertEquals("2 superinterfaces",2,cSig.superInterfaceSignatures.length);
		UnresolvedType comparable = 
			BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
					cSig.superInterfaceSignatures[0],
					cSig.formalTypeParameters,
					world
					);			
		assertEquals("Ljava/lang/Comparable<Ljava/lang/Enum<Ljava/lang/Object;>;>;",comparable.getSignature());
		UnresolvedType serializable = 
			BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
					cSig.superInterfaceSignatures[1],
					cSig.formalTypeParameters,
					world
					);
		assertEquals("Ljava/io/Serializable;",serializable.getSignature());
	}
	
	public void testColonColon() {
		BcelWorld world = new BcelWorld();
		Signature.ClassSignature cSig = new GenericSignatureParser().parseAsClassSignature("<T::Ljava/io/Serializable;>Ljava/lang/Object;Ljava/lang/Comparable<TT;>;");
		UnresolvedType resolved = BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
				cSig.superclassSignature,
				cSig.formalTypeParameters,
				world);
		assertEquals("Ljava/lang/Object;",resolved.getSignature());
		UnresolvedType resolvedInt = BcelGenericSignatureToTypeXConverter.classTypeSignature2TypeX(
				cSig.superInterfaceSignatures[0],
				cSig.formalTypeParameters,
				world);
		
	}
	
}
