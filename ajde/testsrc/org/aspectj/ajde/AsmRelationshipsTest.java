
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

import org.aspectj.asm.*;

// TODO: check for return types
public class AsmRelationshipsTest extends AjdeTestCase {
  
	private AsmManager manager = null;
	private static final String CONFIG_FILE_PATH = "../examples/coverage/coverage.lst";

	public AsmRelationshipsTest(String name) {
		super(name);
	}
  
	public void testInterTypeDeclarations() {		
		checkInterTypeMapping("InterTypeDecCoverage", "Point", "Point.xxx", "Point", 
			"declared on", "aspect declarations", IProgramElement.Kind.INTER_TYPE_FIELD);	
		checkInterTypeMapping("InterTypeDecCoverage", "Point", "Point.check(int, Line)", 
			"Point", "declared on", "aspect declarations", IProgramElement.Kind.INTER_TYPE_METHOD);	
	}

	public void testAdvice() {	
		checkMapping("AdvisesRelationshipCoverage", "Point", "before(): methodExecutionP..", 
			"setX(int)", "advises", "advised by");
		checkUniDirectionalMapping("AdvisesRelationshipCoverage", "Point", "before(): getP..", 
			"field-get(int Point.x)", "advises");
		checkUniDirectionalMapping("AdvisesRelationshipCoverage", "Point", "before(): setP..", 
			"field-set(int Point.x)", "advises");	
	}

	private void checkUniDirectionalMapping(String fromType, String toType, String from, 
		String to, String relName) {
		
		IProgramElement aspect = AsmManager.getDefault().getHierarchy().findElementForType(null, fromType);
		assertNotNull(aspect);		
		String beforeExec = from;
		IProgramElement beforeExecNode = manager.getHierarchy().findElementForLabel(aspect, IProgramElement.Kind.ADVICE, beforeExec);
		assertNotNull(beforeExecNode);
		IRelationship rel = manager.getRelationshipMap().get(beforeExecNode, IRelationship.Kind.ADVICE, relName);
		for (Iterator it = rel.getTargets().iterator(); it.hasNext(); ) {
			String currHandle = (String)it.next();
			if (manager.getHierarchy().findElementForHandle(currHandle).toLabelString().equals(to)) return;
		}
		fail(); // didn't find it
	}

	private void checkMapping(String fromType, String toType, String from, String to, 
		String forwardRelName, String backRelName) {
		
		IProgramElement aspect = AsmManager.getDefault().getHierarchy().findElementForType(null, fromType);
		assertNotNull(aspect);		
		String beforeExec = from;
		IProgramElement beforeExecNode = manager.getHierarchy().findElementForLabel(aspect, IProgramElement.Kind.ADVICE, beforeExec);
		assertNotNull(beforeExecNode);
		IRelationship rel = manager.getRelationshipMap().get(beforeExecNode, IRelationship.Kind.ADVICE, forwardRelName);
		String handle = (String)rel.getTargets().get(0);
		assertEquals(manager.getHierarchy().findElementForHandle(handle).toString(), to);  

		IProgramElement clazz = AsmManager.getDefault().getHierarchy().findElementForType(null, toType);
		assertNotNull(clazz);
		String set = to;
		IProgramElement setNode = manager.getHierarchy().findElementForLabel(clazz, IProgramElement.Kind.METHOD, set);
		assertNotNull(setNode);
		IRelationship rel2 = manager.getRelationshipMap().get(setNode, IRelationship.Kind.ADVICE, backRelName);
		String handle2 = (String)rel2.getTargets().get(0);
		assertEquals(manager.getHierarchy().findElementForHandle(handle2).toString(), from);
	}

	private void checkInterTypeMapping(String fromType, String toType, String from, 
		String to, String forwardRelName, String backRelName, IProgramElement.Kind declareKind) {
		
		IProgramElement aspect = AsmManager.getDefault().getHierarchy().findElementForType(null, fromType);
		assertNotNull(aspect);		
		String beforeExec = from;
		IProgramElement fromNode = manager.getHierarchy().findElementForLabel(aspect, declareKind, beforeExec);
		assertNotNull(fromNode);
		IRelationship rel = manager.getRelationshipMap().get(fromNode, IRelationship.Kind.DECLARE_INTER_TYPE, forwardRelName);
		String handle = (String)rel.getTargets().get(0);
		assertEquals(manager.getHierarchy().findElementForHandle(handle).toString(), to);  

		IProgramElement clazz = AsmManager.getDefault().getHierarchy().findElementForType(null, toType);
		assertNotNull(clazz);
		String set = to;
		IRelationship rel2 = manager.getRelationshipMap().get(clazz, IRelationship.Kind.DECLARE_INTER_TYPE, backRelName);
		String handle2 = (String)rel2.getTargets().get(0);
		for (Iterator it = rel2.getTargets().iterator(); it.hasNext(); ) {
			String currHandle = (String)it.next();
			if (manager.getHierarchy().findElementForHandle(currHandle).toLabelString().equals(from)) return;
		}
		fail(); // didn't find it
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
