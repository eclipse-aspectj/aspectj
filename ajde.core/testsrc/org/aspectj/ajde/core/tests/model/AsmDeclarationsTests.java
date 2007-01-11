/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.ajde.core.tests.model;

import java.io.File;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;

public class AsmDeclarationsTests extends AjdeCoreTestCase {

	private IHierarchy model = null;

	private String[] files = new String[]{
			"ModelCoverage.java",
			"pkg" + File.separator + "InPackage.java"
	};
	
	private TestMessageHandler handler;
	private TestCompilerConfiguration compilerConfig;

	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("coverage");
		handler = (TestMessageHandler) getCompiler().getMessageHandler();
		compilerConfig = (TestCompilerConfiguration) getCompiler()
				.getCompilerConfiguration();
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		doBuild();
		model = AsmManager.getDefault().getHierarchy();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		compilerConfig = null;
		model = null;
	}
	
	public void testRoot() {
		IProgramElement root = (IProgramElement)model.getRoot();
		assertNotNull(root);
		assertEquals("Expected root to be named 'coverage' but found "
				+ root.toLabelString(), root.toLabelString(), "coverage");	
	}
	
	public void testAspectAccessibility() {
		IProgramElement packageAspect = AsmManager.getDefault().getHierarchy().findElementForType(null, "AdviceNamingCoverage");
		assertNotNull(packageAspect);
		assertEquals(IProgramElement.Accessibility.PACKAGE, packageAspect.getAccessibility());
		assertEquals("aspect should not have public in it's signature","aspect AdviceNamingCoverage",packageAspect.getSourceSignature());
	}
	
	public void testStaticModifiers() {
		IProgramElement aspect = AsmManager.getDefault().getHierarchy().findElementForType(null, "ModifiersCoverage");
		assertNotNull(aspect);

		IProgramElement staticA = model.findElementForSignature(aspect, IProgramElement.Kind.FIELD, "staticA");
		assertTrue(staticA.getModifiers().contains(IProgramElement.Modifiers.STATIC));

		IProgramElement finalA = model.findElementForSignature(aspect, IProgramElement.Kind.FIELD, "finalA");
		assertTrue(!finalA.getModifiers().contains(IProgramElement.Modifiers.STATIC));
		assertTrue(finalA.getModifiers().contains(IProgramElement.Modifiers.FINAL));

	}
	
	public void testFileInPackageAndDefaultPackage() {
		IProgramElement root = model.getRoot();
		assertEquals(root.toLabelString(), "coverage");	
		IProgramElement pkg = (IProgramElement)root.getChildren().get(1);
		assertEquals(pkg.toLabelString(), "pkg");	
		assertEquals(((IProgramElement)pkg.getChildren().get(0)).toLabelString(), "InPackage.java"); 	
		assertEquals(((IProgramElement)root.getChildren().get(0)).toLabelString(), "ModelCoverage.java"); 
	}  

	public void testDeclares() {
		IProgramElement node = (IProgramElement)model.getRoot();
		assertNotNull(node);
	
		IProgramElement aspect = AsmManager.getDefault().getHierarchy().findElementForType(null, "DeclareCoverage");
		assertNotNull(aspect);
		
		String label = "declare error: \"Illegal construct..\"";
		IProgramElement decErrNode = model.findElementForSignature(aspect, IProgramElement.Kind.DECLARE_ERROR, "declare error");
		assertNotNull(decErrNode);
		assertEquals(decErrNode.toLabelString(), label);
		
		String decWarnMessage = "declare warning: \"Illegal call.\"";
		IProgramElement decWarnNode = model.findElementForSignature(aspect, IProgramElement.Kind.DECLARE_WARNING, "declare warning");
		assertNotNull(decWarnNode);
		assertEquals(decWarnNode.toLabelString(), decWarnMessage);	
		
		String decParentsMessage = "declare parents: implements Serializable";
		IProgramElement decParentsNode = model.findElementForSignature(aspect, IProgramElement.Kind.DECLARE_PARENTS, "declare parents");
		assertNotNull(decParentsNode);		
		assertEquals(decParentsNode.toLabelString(), decParentsMessage);		
		// check the next two relative to this one
		int declareIndex = decParentsNode.getParent().getChildren().indexOf(decParentsNode);
		String decParentsPtnMessage = "declare parents: extends Observable";		
		assertEquals(decParentsPtnMessage,((IProgramElement)aspect.getChildren().get(declareIndex+1)).toLabelString());			
		String decParentsTPMessage = "declare parents: extends Observable";	
		assertEquals(decParentsTPMessage,((IProgramElement)aspect.getChildren().get(declareIndex+2)).toLabelString());
		
		String decSoftMessage = "declare soft: SizeException";
		IProgramElement decSoftNode = model.findElementForSignature(aspect, IProgramElement.Kind.DECLARE_SOFT, "declare soft");
		assertNotNull(decSoftNode);		
		assertEquals(decSoftNode.toLabelString(), decSoftMessage);		

		String decPrecMessage = "declare precedence: AdviceCoverage, InterTypeDecCoverage, <type pattern>";
		IProgramElement decPrecNode = model.findElementForSignature(aspect, IProgramElement.Kind.DECLARE_PRECEDENCE, "declare precedence");
		assertNotNull(decPrecNode);		
		assertEquals(decPrecNode.toLabelString(), decPrecMessage);	
	} 

	public void testInterTypeMemberDeclares() {
		IProgramElement node = (IProgramElement)model.getRoot();
		assertNotNull(node);
	
		IProgramElement aspect = AsmManager.getDefault().getHierarchy().findElementForType(null, "InterTypeDecCoverage");
		assertNotNull(aspect);
		
		String fieldMsg = "Point.xxx";
		IProgramElement fieldNode = model.findElementForLabel(aspect, IProgramElement.Kind.INTER_TYPE_FIELD, fieldMsg);
		assertNotNull(fieldNode);		
		assertEquals(fieldNode.toLabelString(), fieldMsg);

		String methodMsg = "Point.check(int,Line)";
		IProgramElement methodNode = model.findElementForLabel(aspect, IProgramElement.Kind.INTER_TYPE_METHOD, methodMsg);
		assertNotNull(methodNode);		
		assertEquals(methodNode.toLabelString(), methodMsg);

		// TODO: enable
//		String constructorMsg = "Point.new(int, int, int)";
//		ProgramElementNode constructorNode = model.findNode(aspect, ProgramElementNode.Kind.INTER_TYPE_CONSTRUCTOR, constructorMsg);
//		assertNotNull(constructorNode);		
//		assertEquals(constructorNode.toLabelString(), constructorMsg);
	}
	
	public void testPointcuts() {
		IProgramElement node = (IProgramElement)model.getRoot();
		assertNotNull(node);
	
		IProgramElement aspect = AsmManager.getDefault().getHierarchy().findElementForType(null, "AdviceNamingCoverage");
		assertNotNull(aspect);		
	
		String ptct = "named()";
		IProgramElement ptctNode = model.findElementForSignature(aspect, IProgramElement.Kind.POINTCUT, ptct);
		assertNotNull(ptctNode);		
		assertEquals(ptctNode.toLabelString(), ptct);		

		String params = "namedWithArgs(int,int)";
		IProgramElement paramsNode = model.findElementForSignature(aspect, IProgramElement.Kind.POINTCUT, params);
		assertNotNull(paramsNode);		
		assertEquals(paramsNode.toLabelString(), params);	
	}

	public void testAbstract() {
		IProgramElement node = (IProgramElement)model.getRoot();
		assertNotNull(node);
	
		IProgramElement aspect = AsmManager.getDefault().getHierarchy().findElementForType(null, "AbstractAspect");
		assertNotNull(aspect);	
		
		String abst = "abPtct()";
		IProgramElement abstNode = model.findElementForSignature(aspect, IProgramElement.Kind.POINTCUT, abst);
		assertNotNull(abstNode);		
		assertEquals(abstNode.toLabelString(), abst);			
	}

	public void testAdvice() {
		IProgramElement node = (IProgramElement)model.getRoot();
		assertNotNull(node);
	
		IProgramElement aspect = AsmManager.getDefault().getHierarchy().findElementForType(null, "AdviceNamingCoverage");
		assertNotNull(aspect);	

		String anon = "before(): <anonymous pointcut>";
		IProgramElement anonNode = model.findElementForLabel(aspect, IProgramElement.Kind.ADVICE, anon);
		assertNotNull(anonNode);		
		assertEquals(anonNode.toLabelString(), anon);			

		String named = "before(): named..";
		IProgramElement namedNode = model.findElementForLabel(aspect, IProgramElement.Kind.ADVICE, named);
		assertNotNull(namedNode);		
		assertEquals(namedNode.toLabelString(), named);		

		String namedWithOneArg = "around(int): namedWithOneArg..";
		IProgramElement namedWithOneArgNode = model.findElementForLabel(aspect, IProgramElement.Kind.ADVICE, namedWithOneArg);
		assertNotNull(namedWithOneArgNode);		
		assertEquals(namedWithOneArgNode.toLabelString(), namedWithOneArg);		

		String afterReturning = "afterReturning(int,int): namedWithArgs..";
		IProgramElement afterReturningNode = model.findElementForLabel(aspect, IProgramElement.Kind.ADVICE, afterReturning);
		assertNotNull(afterReturningNode);		
		assertEquals(afterReturningNode.toLabelString(), afterReturning);

		String around = "around(int): namedWithOneArg..";
		IProgramElement aroundNode = model.findElementForLabel(aspect, IProgramElement.Kind.ADVICE, around);
		assertNotNull(aroundNode);		
		assertEquals(aroundNode.toLabelString(), around);

		String compAnon = "before(int): <anonymous pointcut>..";
		IProgramElement compAnonNode = model.findElementForLabel(aspect, IProgramElement.Kind.ADVICE, compAnon);
		assertNotNull(compAnonNode);		
		assertEquals(compAnonNode.toLabelString(), compAnon);

		String compNamed = "before(int): named()..";
		IProgramElement compNamedNode = model.findElementForLabel(aspect, IProgramElement.Kind.ADVICE, compNamed);
		assertNotNull(compNamedNode);		
		assertEquals(compNamedNode.toLabelString(), compNamed);
	}
	
}
