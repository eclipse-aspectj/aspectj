
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
import org.aspectj.asm.ProgramElementNode.Kind;


// TODO: check for return types
public class AsmDeclarationsTest extends AjdeTestCase {

	private StructureModel model = null;
	private static final String CONFIG_FILE_PATH = "../examples/coverage/coverage.lst";
	private static final int DEC_MESSAGE_LENGTH = AsmNodeFormatter.MAX_MESSAGE_LENGTH;

	public AsmDeclarationsTest(String name) {
		super(name);
	}

	public void testDeclares() {
		ProgramElementNode node = (ProgramElementNode)model.getRoot();
		assertNotNull(node);
	
		ProgramElementNode aspect = StructureModelManager.getDefault().getStructureModel().findNodeForClass(null, "InterTypeDecCoverage");
		assertNotNull(aspect);
		
		String decErrMessage = "declare error: \"Illegal construct..\"";
		ProgramElementNode decErrNode = model.findNode(aspect, ProgramElementNode.Kind.DECLARE_ERROR, decErrMessage);
		assertNotNull(decErrNode);
		assertEquals(decErrNode.getName(), decErrMessage);
		
		String decWarnMessage = "declare warning: \"Illegal construct..\"";
		ProgramElementNode decWarnNode = model.findNode(aspect, ProgramElementNode.Kind.DECLARE_WARNING, decWarnMessage);
		assertNotNull(decWarnNode);
		assertEquals(decWarnNode.getName(), decWarnMessage);	
		
		String decParentsMessage = "declare parents: Point";
		ProgramElementNode decParentsNode = model.findNode(aspect, ProgramElementNode.Kind.DECLARE_PARENTS, decParentsMessage);
		assertNotNull(decParentsNode);		
		assertEquals(decParentsNode.getName(), decParentsMessage);	
			
		String decParentsPtnMessage = "declare parents: Point+";
		ProgramElementNode decParentsPtnNode = model.findNode(aspect, ProgramElementNode.Kind.DECLARE_PARENTS, decParentsPtnMessage);
		assertNotNull(decParentsPtnNode);		
		assertEquals(decParentsPtnNode.getName(), decParentsPtnMessage);			

		String decParentsTPMessage = "declare parents: <type pattern>";
		ProgramElementNode decParentsTPNode = model.findNode(aspect, ProgramElementNode.Kind.DECLARE_PARENTS, decParentsTPMessage);
		assertNotNull(decParentsTPNode);		
		assertEquals(decParentsTPNode.getName(), decParentsTPMessage);
		
		String decSoftMessage = "declare soft: SizeException";
		ProgramElementNode decSoftNode = model.findNode(aspect, ProgramElementNode.Kind.DECLARE_SOFT, decSoftMessage);
		assertNotNull(decSoftNode);		
		assertEquals(decSoftNode.getName(), decSoftMessage);		

		String decPrecMessage = "declare precedence: AdviceCoverage, InterTypeDecCoverage, <type pattern>";
		ProgramElementNode decPrecNode = model.findNode(aspect, ProgramElementNode.Kind.DECLARE_PRECEDENCE, decPrecMessage);
		assertNotNull(decPrecNode);		
		assertEquals(decPrecNode.getName(), decPrecMessage);	
	} 

	public void testInterTypeMemberDeclares() {
		ProgramElementNode node = (ProgramElementNode)model.getRoot();
		assertNotNull(node);
	
		ProgramElementNode aspect = StructureModelManager.getDefault().getStructureModel().findNodeForClass(null, "InterTypeDecCoverage");
		assertNotNull(aspect);
		
		String fieldMsg = "Point.xxx";
		ProgramElementNode fieldNode = model.findNode(aspect, ProgramElementNode.Kind.INTER_TYPE_FIELD, fieldMsg);
		assertNotNull(fieldNode);		
		assertEquals(fieldNode.getName(), fieldMsg);

		String methodMsg = "Point.check(int, Line)";
		ProgramElementNode methodNode = model.findNode(aspect, ProgramElementNode.Kind.INTER_TYPE_METHOD, methodMsg);
		assertNotNull(methodNode);		
		assertEquals(methodNode.getName(), methodMsg);

		// TODO: enable
//		String constructorMsg = "Point.new(int, int, int)";
//		ProgramElementNode constructorNode = model.findNode(aspect, ProgramElementNode.Kind.INTER_TYPE_CONSTRUCTOR, constructorMsg);
//		assertNotNull(constructorNode);		
//		assertEquals(constructorNode.getName(), constructorMsg);
	}

	public void testPointcuts() {
		ProgramElementNode node = (ProgramElementNode)model.getRoot();
		assertNotNull(node);
	
		ProgramElementNode aspect = StructureModelManager.getDefault().getStructureModel().findNodeForClass(null, "AdviceNamingCoverage");
		assertNotNull(aspect);		
	
		String ptct = "named()";
		ProgramElementNode ptctNode = model.findNode(aspect, ProgramElementNode.Kind.POINTCUT, ptct);
		assertNotNull(ptctNode);		
		assertEquals(ptctNode.getName(), ptct);		

		String params = "namedWithArgs(int, int)";
		ProgramElementNode paramsNode = model.findNode(aspect, ProgramElementNode.Kind.POINTCUT, params);
		assertNotNull(paramsNode);		
		assertEquals(paramsNode.getName(), params);	


	}

	public void testAbstract() {
		ProgramElementNode node = (ProgramElementNode)model.getRoot();
		assertNotNull(node);
	
		ProgramElementNode aspect = StructureModelManager.getDefault().getStructureModel().findNodeForClass(null, "AbstractAspect");
		assertNotNull(aspect);	
		
		String abst = "abPtct()";
		ProgramElementNode abstNode = model.findNode(aspect, ProgramElementNode.Kind.POINTCUT, abst);
		assertNotNull(abstNode);		
		assertEquals(abstNode.getName(), abst);			
	}

	public void testAdvice() {
		ProgramElementNode node = (ProgramElementNode)model.getRoot();
		assertNotNull(node);
	
		ProgramElementNode aspect = StructureModelManager.getDefault().getStructureModel().findNodeForClass(null, "AdviceNamingCoverage");
		assertNotNull(aspect);	

		String anon = "before(): <anonymous pointcut>";
		ProgramElementNode anonNode = model.findNode(aspect, ProgramElementNode.Kind.ADVICE, anon);
		assertNotNull(anonNode);		
		assertEquals(anonNode.getName(), anon);			

		String named = "before(): named..";
		ProgramElementNode namedNode = model.findNode(aspect, ProgramElementNode.Kind.ADVICE, named);
		assertNotNull(namedNode);		
		assertEquals(namedNode.getName(), named);		

		String namedWithOneArg = "around(int): namedWithOneArg..";
		ProgramElementNode namedWithOneArgNode = model.findNode(aspect, ProgramElementNode.Kind.ADVICE, namedWithOneArg);
		assertNotNull(namedWithOneArgNode);		
		assertEquals(namedWithOneArgNode.getName(), namedWithOneArg);		

		String afterReturning = "afterReturning(int, int): namedWithArgs..";
		ProgramElementNode afterReturningNode = model.findNode(aspect, ProgramElementNode.Kind.ADVICE, afterReturning);
		assertNotNull(afterReturningNode);		
		assertEquals(afterReturningNode.getName(), afterReturning);

		String around = "around(int): namedWithOneArg..";
		ProgramElementNode aroundNode = model.findNode(aspect, ProgramElementNode.Kind.ADVICE, around);
		assertNotNull(aroundNode);		
		assertEquals(aroundNode.getName(), around);

		String compAnon = "before(int): <anonymous pointcut>..";
		ProgramElementNode compAnonNode = model.findNode(aspect, ProgramElementNode.Kind.ADVICE, compAnon);
		assertNotNull(compAnonNode);		
		assertEquals(compAnonNode.getName(), compAnon);

		String compNamed = "before(int): named()..";
		ProgramElementNode compNamedNode = model.findNode(aspect, ProgramElementNode.Kind.ADVICE, compNamed);
		assertNotNull(compNamedNode);		
		assertEquals(compNamedNode.getName(), compNamed);
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
