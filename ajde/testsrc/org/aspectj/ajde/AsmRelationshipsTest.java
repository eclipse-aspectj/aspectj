
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

import java.util.Iterator;

import org.aspectj.ajdt.internal.core.builder.AsmNodeFormatter;
import org.aspectj.asm.*;
import org.aspectj.asm.IProgramElement.Kind;


// TODO: check for return types
public class AsmRelationshipsTest extends AjdeTestCase {

	private StructureModel model = null;
	private static final String CONFIG_FILE_PATH = "../examples/coverage/coverage.lst";

	public AsmRelationshipsTest(String name) {
		super(name);
	}

	public void testExecution() {
		IProgramElement node = (IProgramElement)model.getRoot();
		assertNotNull(node);
	
		IProgramElement aspect = StructureModelManager.getDefault().getStructureModel().findNodeForClass(null, "AdvisesRelationshipCoverage");
		assertNotNull(aspect);		

		String beforeExec = "before(): executionP..";
		IProgramElement beforeExecNode = model.findNode(aspect, IProgramElement.Kind.ADVICE, beforeExec);
		assertNotNull(beforeExecNode);
		
		
		
//		System.err.println("> root: " + node);
	
				
//		assertEquals(ptctNode.getName(), ptct);	
		
//	
//		IProgramElement aspect = StructureModelManager.getDefault().getStructureModel().findNodeForClass(null, "AdviceNamingCoverage");
//		assertNotNull(aspect);		
	
//		fail();
		
	
//
//		String params = "namedWithArgs(int, int)";
//		IProgramElement paramsNode = model.findNode(aspect, IProgramElement.Kind.POINTCUT, params);
//		assertNotNull(paramsNode);		
//		assertEquals(paramsNode.getName(), params);	
	}

	protected void setUp() throws Exception {
		super.setUp("examples");
		assertTrue("build success", doSynchronousBuild(CONFIG_FILE_PATH));	
		model =	StructureModelManager.getDefault().getStructureModel();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
