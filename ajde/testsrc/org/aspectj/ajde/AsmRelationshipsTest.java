
/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * ******************************************************************/

package org.aspectj.ajde;

import java.util.List;

import org.aspectj.asm.*;
import org.aspectj.asm.internal.ProgramElement;


// TODO: check for return types
public class AsmRelationshipsTest extends AjdeTestCase {
  
	private AsmManager manager = null;
	private static final String CONFIG_FILE_PATH = "../examples/coverage/coverage.lst";

	public AsmRelationshipsTest(String name) {
		super(name);
	}
  
//	public void testInterTypeDeclarations() {		
//		checkMapping("InterTypeDecCoverage", "Point", "Point.xxx:", "xxx");	
//	}

	public void testAdvice() {	
		checkMapping("AdvisesRelationshipCoverage", "Point", "before(): methodExecutionP..", "setX(int)");
		checkUniDirectionalMapping("AdvisesRelationshipCoverage", "Point", "before(): getP..", "field-get(int Point.x)");
		checkUniDirectionalMapping("AdvisesRelationshipCoverage", "Point", "before(): setP..", "field-set(int Point.xxx)");	
	}

	private void checkUniDirectionalMapping(String fromType, String toType, String from, String to) {
		IProgramElement aspect = AsmManager.getDefault().getModel().findNodeForType(null, fromType);
		assertNotNull(aspect);		
		String beforeExec = from;
		IProgramElement beforeExecNode = manager.getModel().findNode(aspect, IProgramElement.Kind.ADVICE, beforeExec);
		assertNotNull(beforeExecNode);
		IRelationship rel = manager.getMapper().get(beforeExecNode);
		assertEquals(((IProgramElement)rel.getTargets().get(0)).getName(), to);
	}

	private void checkMapping(String fromType, String toType, String from, String to) {
		IProgramElement aspect = AsmManager.getDefault().getModel().findNodeForType(null, fromType);
		assertNotNull(aspect);		
		String beforeExec = from;
		IProgramElement beforeExecNode = manager.getModel().findNode(aspect, IProgramElement.Kind.ADVICE, beforeExec);
		assertNotNull(beforeExecNode);
		IRelationship rel = manager.getMapper().get(beforeExecNode);
		assertEquals(((IProgramElement)rel.getTargets().get(0)).getName(), to);

		IProgramElement clazz = AsmManager.getDefault().getModel().findNodeForType(null, toType);
		assertNotNull(clazz);
		String set = to;
		IProgramElement setNode = manager.getModel().findNode(clazz, IProgramElement.Kind.METHOD, set);
		assertNotNull(setNode);
		IRelationship rel2 = manager.getMapper().get(setNode);
		assertEquals(((IProgramElement)rel2.getTargets().get(0)).getName(), from);
	}

	protected void setUp() throws Exception {
		super.setUp("examples");
		assertTrue("build success", doSynchronousBuild(CONFIG_FILE_PATH));	
		manager = AsmManager.getDefault();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
