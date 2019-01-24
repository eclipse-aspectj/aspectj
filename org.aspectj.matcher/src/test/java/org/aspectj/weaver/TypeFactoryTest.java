/* *******************************************************************
 * Copyright (c) 2010 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver;

import junit.framework.TestCase;

/**
 * Check signature to type mapping.
 * 
 * @author Andy Clement
 */
public class TypeFactoryTest extends TestCase {

	public void testParameterizedSig() {
		UnresolvedType t = null;
		t = UnresolvedType.forSignature("Pjava/util/List<Ljava/lang/String;>;");
		assertEquals("Ljava/util/List;", t.getErasureSignature());
		assertEquals("Ljava/lang/String;", t.getTypeParameters()[0].signature);
		assertEquals("Ljava/lang/String;", t.getTypeParameters()[0].signatureErasure);
		assertEquals("Pjava/util/List<Ljava/lang/String;>;", t.getSignature());

		t = TypeFactory.createTypeFromSignature("Ljava/util/List<Ljava/lang/String;>;");
		assertEquals("Ljava/util/List;", t.getErasureSignature());
		assertEquals("Ljava/lang/String;", t.getTypeParameters()[0].signature);
		assertEquals("Ljava/lang/String;", t.getTypeParameters()[0].signatureErasure);
		assertEquals("Pjava/util/List<Ljava/lang/String;>;", t.getSignature());

		t = UnresolvedType.forName("java.util.List<java.lang.String>");
		assertEquals("Ljava/util/List;", t.getErasureSignature());
		assertEquals("Ljava/lang/String;", t.getTypeParameters()[0].signature);
		assertEquals("Ljava/lang/String;", t.getTypeParameters()[0].signatureErasure);
		assertEquals("Pjava/util/List<Ljava/lang/String;>;", t.getSignature());

		t = UnresolvedType.forSignature("Pjava/util/Map<TS;Pjava/util/List<Ljava/lang/String;>;>;");
		assertEquals("Ljava/util/Map;", t.getErasureSignature());
		assertEquals("TS;", t.getTypeParameters()[0].signature);
		assertEquals("Ljava/lang/Object;", t.getTypeParameters()[0].signatureErasure);
		assertEquals("S", ((UnresolvedTypeVariableReferenceType) t.getTypeParameters()[0]).getTypeVariable().getName());
		assertEquals("Pjava/util/Map<TS;Pjava/util/List<Ljava/lang/String;>;>;", t.getSignature());
		assertEquals("Pjava/util/List<Ljava/lang/String;>;", t.getTypeParameters()[1].signature);
		assertEquals("Ljava/util/List;", t.getTypeParameters()[1].signatureErasure);

		t = UnresolvedType.forSignature("Pjava/util/List<+Pnl/ZoekFoo<TS;Pnl/ZoekCopy<TS;>;>;>;");
		assertEquals("Ljava/util/List;", t.getErasureSignature());
		WildcardedUnresolvedType wut = (WildcardedUnresolvedType) t.getTypeParameters()[0];
		assertEquals("+Pnl/ZoekFoo<TS;Pnl/ZoekCopy<TS;>;>;", wut.signature);
		assertEquals("Lnl/ZoekFoo;", wut.signatureErasure);
		assertTrue(wut.isExtends());
		assertEquals("Pnl/ZoekFoo<TS;Pnl/ZoekCopy<TS;>;>;", wut.getUpperBound().signature);
		assertEquals("Lnl/ZoekFoo;", wut.getUpperBound().signatureErasure);
		UnresolvedTypeVariableReferenceType tvar = (UnresolvedTypeVariableReferenceType) wut.getUpperBound().getTypeParameters()[0];
		assertEquals("Pnl/ZoekFoo<TS;Pnl/ZoekCopy<TS;>;>;", wut.getUpperBound().signature);
		assertEquals("Lnl/ZoekFoo;", wut.getUpperBound().signatureErasure);
		assertEquals("S", tvar.getTypeVariable().getName());
		UnresolvedType t2 = wut.getUpperBound().getTypeParameters()[1];
		assertEquals("Pnl/ZoekCopy<TS;>;", t2.getSignature());
		assertEquals("Lnl/ZoekCopy;", t2.getErasureSignature());

		//		//		t = UnresolvedType.forSignature("Ljava/util/List<+Lnl/ZoekFoo<TS;Lnl/ZoekCopy<TS;>;>;>;");
		//		t = TypeFactory.createTypeFromSignature("Ljava/util/List<+Lnl/ZoekFoo<TS;Lnl/ZoekCopy<TS;>;>;>;");
		//		System.out.println(t.getSignature());
		//
		//		t = TypeFactory.createTypeFromSignature("Ljava/util/List<Lnl/ZoekFoo<Ljava/lang/String;>;>;");
		//		System.out.println(t.getSignature()); // Pjava/util/List<Lnl/ZoekFoo<Ljava/lang/String;>;>;

		// TODO should be able to cope with nested parameterizations
		// Foo<String>.Bar<List<Map<String,Integer>>>
		// both components Foo and Bar of that are parameterized
	}
}
