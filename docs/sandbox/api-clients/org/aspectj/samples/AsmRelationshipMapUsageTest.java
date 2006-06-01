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

import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.asm.*;

/**
 * Collects join point information for all advised methods and constructors.  
 * 
 * @author Mik Kersten
 */
public class AsmRelationshipMapUsageTest extends AjdeTestCase {

    public void testFindAdvisedMethods() {        
        System.out.println("----------------------------------");
        System.out.println("Methods affected by advice: ");
        HierarchyWalker walker = new HierarchyWalker() {
            public void preProcess(IProgramElement node) {
                if (node.getKind().equals(IProgramElement.Kind.METHOD)) {
                    List relations = AsmManager.getDefault().getRelationshipMap().get(node);
                    if (relations != null) {
	                    for (Iterator it = relations.iterator(); it.hasNext(); ) {
	                        IRelationship relationship = (IRelationship)it.next();
	                        if (relationship.getKind().equals(IRelationship.Kind.ADVICE)) {
	                            System.out.println(
	                                    "method: " + node.toString() 
	                                    + ", advised by: " + relationship.getTargets());
	                        } 
	                    }
                    }
                }
            }
        };
        AsmManager.getDefault().getHierarchy().getRoot().walk(walker);
    }
    
    public void testListFilesAffectedByInterTypeDecs() {
        System.out.println("----------------------------------");
        System.out.println("Files affected by inter type declarations: ");
        HierarchyWalker walker = new HierarchyWalker() {
            public void preProcess(IProgramElement node) {
                if (node.getKind().equals(IProgramElement.Kind.CLASS)) {
                    List relations = AsmManager.getDefault().getRelationshipMap().get(node);
                    if (relations != null) {
	                    for (Iterator it = relations.iterator(); it.hasNext(); ) {
	                        IRelationship relationship = (IRelationship)it.next();
	                        if (relationship.getKind().equals(IRelationship.Kind.DECLARE_INTER_TYPE)) {
	                            System.out.println(
                                    "file: " + node.getSourceLocation().getSourceFile().getName() 
                                    + ", declared on by: " + relationship.getTargets());
	                        } 
	                    }
                    }
                }
            }
        };
        AsmManager.getDefault().getHierarchy().getRoot().walk(walker);
    }
    
	
	protected void setUp() throws Exception {
	    super.setUp("examples");
		assertTrue("build success", doSynchronousBuild("../examples/spacewar/spacewar/debug.lst"));	
	}
}

