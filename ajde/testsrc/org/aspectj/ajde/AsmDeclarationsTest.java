
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
		
		String decErrMessage = "declare error: Illegal construct..";
		ProgramElementNode decErrNode = model.findNode(aspect, ProgramElementNode.Kind.DECLARE_ERROR, decErrMessage);
		assertNotNull(decErrNode);
		assertEquals(decErrNode.getName(), decErrMessage);
		
		String decWarnMessage = "declare warning: Illegal construct..";
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

	public void testAdvice() {
		ProgramElementNode node = (ProgramElementNode)model.getRoot();
		assertNotNull(node);
	
		ProgramElementNode aspect = StructureModelManager.getDefault().getStructureModel().findNodeForClass(null, "AdviceNamingCoverage");
		assertNotNull(aspect);	

//		String anon = "<anonymous pointcut>";
//		ProgramElementNode anonNode = model.findNode(aspect, ProgramElementNode.Kind.POINTCUT, anon);
//		assertNotNull(anonNode);		
//		assertEquals(anonNode.getName(), anon);			
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
