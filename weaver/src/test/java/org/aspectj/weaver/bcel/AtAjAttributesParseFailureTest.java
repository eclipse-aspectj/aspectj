/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.weaver.bcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.UnresolvedType;

import junit.framework.TestCase;

public class AtAjAttributesParseFailureTest extends TestCase {

	/**
	 * Reads a class carrying @DeclarePrecedence(value) and returns the errors
	 * reported.
	 */
	private IMessage[] errorsReadingPrecedence(String value) {
		ClassGen cg = new ClassGen("crafted.Probe", "java.lang.Object", "Probe.java", Constants.ACC_PUBLIC, null);
		ConstantPool cp = cg.getConstantPool();
		List<NameValuePair> pairs = new ArrayList<>();
		pairs.add(new NameValuePair("value", new SimpleElementValue(ElementValue.STRING, cp, value), cp));
		cg.addAnnotation(
				new AnnotationGen(new ObjectType("org.aspectj.lang.annotation.DeclarePrecedence"), pairs, true, cp));

		MessageHandler handler = new MessageHandler();
		BcelWorld world = new BcelWorld();
		world.setMessageHandler(handler);
		ReferenceType type = (ReferenceType) world.resolve(UnresolvedType.forName(getClass().getName()));
		AtAjAttributes.readAj5ClassAttributes(null, cg.getJavaClass(), type, type.getSourceContext(), handler, false);
		return handler.getErrors();
	}

	public void testValidPrecedenceIsAccepted() {
		assertEquals(0, errorsReadingPrecedence("*").length);
	}

	public void testInvalidPrecedenceIsReportedNotThrown() {
		IMessage[] errors = errorsReadingPrecedence("(");
		assertEquals(1, errors.length);
		assertTrue(errors[0].getMessage(), errors[0].getMessage().contains("Invalid @DeclarePrecedence"));
	}

	public void testDeeplyNestedPrecedenceIsReportedNotThrown() {
		char[] bangs = new char[20000];
		Arrays.fill(bangs, '!');
		IMessage[] errors = errorsReadingPrecedence(new String(bangs) + "*");
		assertEquals(1, errors.length);
		assertTrue(errors[0].getMessage(), errors[0].getMessage().contains("Invalid @DeclarePrecedence"));
		assertTrue("message must not echo the whole pattern", errors[0].getMessage().length() < 1000);
	}
}