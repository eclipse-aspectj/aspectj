/* *******************************************************************
 * Copyright (c) 2004 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
 package org.aspectj.samples;

import java.util.ArrayList;

import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
  
/**
 * This test demonstrates how hierarchy building in the ASM can be extended
 * to put additional information in the model, for example method call sites.
 * 
 * @author Mik Kersten
 */
public class AsmHierarchyBuilderExtensionTest extends AjdeTestCase {

    private ExtendedAsmHiearchyBuilder builder = new ExtendedAsmHiearchyBuilder();
    
	public void testHiearchyExtension() {
	    assertNotNull(AsmManager.getDefault().getHierarchy().getRoot());
	    System.out.println(AsmManager.getDefault().getHierarchy().getRoot().toLongString());
	}
	
	protected void setUp() throws Exception {
		super.setUp("examples");
		AjBuildManager.setAsmHierarchyBuilder(builder);  // NOTE that we set our builder here
		assertTrue("build success", doSynchronousBuild("../examples/coverage/coverage.lst"));	
	}
}

class ExtendedAsmHiearchyBuilder extends AsmHierarchyBuilder {
    
    public boolean visit(MessageSend messageSend, BlockScope scope) {
		IProgramElement peNode = new ProgramElement(
				new String(">>> found call: " + messageSend.toString()),
				IProgramElement.Kind.CODE,	
				null, //makeLocation(messageSend),
				0,
				"",
				new ArrayList());
//			peNode.setCorrespondingType(messageSend.typ  ieldDeclaration.type.toString());
//			peNode.setSourceSignature(genSourceSignature(fieldDeclaration));
			((IProgramElement)stack.peek()).addChild(peNode);
			stack.push(peNode);
			return true;
    }
    public void endVisit(MessageSend messageSend, BlockScope scope) {
        stack.pop();
    }

}
