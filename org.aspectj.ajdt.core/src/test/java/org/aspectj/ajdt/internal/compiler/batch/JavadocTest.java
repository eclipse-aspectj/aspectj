/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;

public class JavadocTest extends AjcTestCase {
	public static final String PROJECT_DIR = "javadoc";

	private File baseDir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		baseDir = new File("../org.aspectj.ajdt.core/testdata", PROJECT_DIR);
	}

	/**
	 * Aim: Check javadoc warning that appear are appropriate
	 * 
	 * ajc -warn:allJavadoc World.java
	 * 
	 */
	public void testMissingJavadoc() {
		String[] args = new String[] { "World.java", "-warn:allJavadoc", "-1.4" };

		List<Message> warningMessages = new ArrayList<>();
		// These warnings are against public textX() methods declared in the World.java
		// type. These test methods are spread between AJ constructs, meaning
		// if someone messes up and the javadoc is not associated with the aspectj
		// construct then it will associated by accident with one of the testX() methods.
		// By checking we get a warning against every testX() method, we are verifying
		// that the javadoc is being attached to the aspectj constructs.
		warningMessages.add(new Message(10, "Missing comment for public declaration"));
		warningMessages.add(new Message(18, "Missing comment for public declaration"));
		warningMessages.add(new Message(28, "Missing comment for public declaration"));
		warningMessages.add(new Message(36, "Missing comment for public declaration"));
		warningMessages.add(new Message(44, "Missing comment for public declaration"));
		warningMessages.add(new Message(53, "Missing comment for public declaration"));
		warningMessages.add(new Message(61, "Missing comment for public declaration"));
		warningMessages.add(new Message(69, "Missing comment for public declaration"));
		// TODO why don't see these for the other ones that have the same problem?
		// Basically that the javadoc on a public member refers to something that is not public
		warningMessages.add(new Message(6,"'public' visibility for malformed doc comments hides this 'default' reference"));
		warningMessages.add(new Message(32,"'public' visibility for malformed doc comments hides this 'default' reference"));
		warningMessages.add(new Message(22,"'public' visibility for malformed doc comments hides this 'default' reference"));
		warningMessages.add(new Message(48,"'public' visibility for malformed doc comments hides this 'default' reference"));
		MessageSpec spec = new MessageSpec(warningMessages, null);

		CompilationResult result = ajc(baseDir, args);
		assertMessages(result, spec);

		// dump(result.getWarningMessages());
		// System.err.println("-----------\n"+ajc.getLastCompilationResult().getStandardError());
		// List l = result.getWarningMessages();
		// IMessage m = ((IMessage)l.get(0));
	}

	// private void dump(List l) {
	// for (Iterator iter = l.iterator(); iter.hasNext();) {
	// IMessage element = (IMessage) iter.next();
	// System.err.println("Warning: "+element);
	// }
	// }
}
