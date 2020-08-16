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
package org.aspectj.ajde;

import java.io.File;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;

/**
 * Tests the 'extensions' to AJDE: 1) ID is now available on messages to allow you to see what 'kind' of message it is - this
 * activates quick fixes/etc in Eclipse.
 */
public class ExtensionTest extends AjcTestCase {

	public static final String PROJECT_DIR = "extensions";
	private static final boolean debugTests = false;
	private File baseDir;

	protected void setUp() throws Exception {
		super.setUp();
		// TODO-path
		baseDir = new File("../ajde/testdata", PROJECT_DIR);
	}

	/**
	 * Aim: Check that the ID of certain message kinds are correct
	 * 
	 * ajc -warn:unusedImport UnusedImport.java
	 * 
	 * Expected result is that id matches IProblem.UnusedImport
	 */
	public void testMessageID() {
		String[] args = new String[] { "UnusedImport.java", "-warn:unusedImport" };
		CompilationResult result = ajc(baseDir, args);
		List l = result.getWarningMessages();
		IMessage m = ((IMessage) l.get(0));
		assertTrue("Expected ID of message to be " + IProblem.UnusedImport + " (UnusedImport) but found an ID of " + m.getID(),
				m.getID() == IProblem.UnusedImport);
	}

	public void testInnerClassesInASM() {
		String[] args = new String[] { "InnerClasses.java", "-emacssym", "-Xset:minimalModel=false" };
		CompilationResult result = ajc(baseDir, args);
		/* List l = */result.getWarningMessages();
		/* Properties p = */AsmManager.lastActiveStructureModel.summarizeModel().getProperties();
		if (debugTests)
			System.out.println("Structure Model for InnerClasses.java:");
		walkit(AsmManager.lastActiveStructureModel.getHierarchy().getRoot(), 0);
		foundNode = null;
		findChild("main", AsmManager.lastActiveStructureModel.getHierarchy().getRoot());
		assertTrue("Should have found node 'main' in the model", foundNode != null);
		IProgramElement runnableChild = getChild(foundNode, "new Runnable() {..}");
		assertTrue("'main' should have a child 'new Runnable() {..}'", runnableChild != null);
		assertTrue("'new Runnable() {..}' should have a 'run' child", getChild(runnableChild, "run") != null);

		/*
		 * Left hand side is before the fix, right hand side is after: <root> InnerClasses.java import declarations InnerClasses A A
		 * method method 1 new Runnable() {..} run run main main 2 new Runnable() {..} run run 3 new Object() {..} toString toString
		 * 4 new Runnable run run
		 */

	}

	private IProgramElement getChild(IProgramElement parent, String s) {
		List<IProgramElement> kids = parent.getChildren();
		for (IProgramElement element : kids) {
			if (element.getName().contains(s))
				return element;
		}
		return null;
	}

	private IProgramElement foundNode = null;

	private void findChild(String s, IProgramElement ipe) {
		if (ipe == null)
			return;
		if (ipe.getName().contains(s)) {
			foundNode = ipe;
			return;
		}
		if (ipe.getChildren() != null) {
			List kids = ipe.getChildren();
			for (Object kid : kids) {
				IProgramElement element = (IProgramElement) kid;
				findChild(s, element);
			}
		}
	}

	public void walkit(IProgramElement ipe, int indent) {
		if (ipe != null) {
			if (debugTests)
				for (int i = 0; i < indent; i++)
					System.out.print(" ");
			if (debugTests)
				System.out.println(ipe.toLabelString());// getName());
			if (ipe.getChildren() != null) {
				List kids = ipe.getChildren();
				for (Object kid : kids) {
					IProgramElement element = (IProgramElement) kid;
					walkit(element, indent + 2);
				}
			}
		}
	}

	/**
	 * Aim: Check that the start/end of certain warnings are correct
	 * 
	 * ajc -warn:unusedImport UnusedImport.java
	 * 
	 * Expected result is first warning message has start=7 end=20
	 */
	public void testMessageSourceStartEnd() {
		String[] args = new String[] { "UnusedImport.java", "-warn:unusedImport" };
		CompilationResult result = ajc(baseDir, args);
		List l = result.getWarningMessages();
		IMessage m = ((IMessage) l.get(0));
		assertTrue("Expected source start to be 7 but was " + m.getSourceStart(), m.getSourceStart() == 7);
		assertTrue("Expected source end to be 20 but was " + m.getSourceEnd(), m.getSourceEnd() == 20);
	}

}
