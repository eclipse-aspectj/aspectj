/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     AMC 21.01.2003 fixed for new source location in eclipse.org
 * ******************************************************************/

package org.aspectj.ajde;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestSuite;

import org.aspectj.asm.ModelWalker;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.asm.StructureModel;
import org.aspectj.asm.StructureNode;

/**
 * @author Mik Kersten
 */
public class StructureModelTest extends AjdeTestCase {
	
	private final String CONFIG_FILE_PATH = "../examples/figures-coverage/all.lst";

	public StructureModelTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(StructureModelTest.class);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(StructureModelTest.class);	
		return result;
	}

//  XXX this should work
//	public void testFieldInitializerCorrespondence() throws IOException {
//		File testFile = createFile("testdata/examples/figures-coverage/figures/Figure.java");	
//		StructureNode node = Ajde.getDefault().getStructureModelManager().getStructureModel().findNodeForSourceLine(
//			testFile.getCanonicalPath(), 28);
//		assertTrue("find result", node != null) ;	
//		ProgramElementNode pNode = (ProgramElementNode)node;
//		ProgramElementNode foundNode = null;
//		final List list = pNode.getRelations();
//		//System.err.println(">>>> " + pNode + ", " + list);
//        assertNotNull("pNode.getRelations()", list);
//		for (Iterator it = list.iterator(); it.hasNext(); ) {
//			RelationNode relation = (RelationNode)it.next();
//			
//			if (relation.getRelation().equals(AdviceAssociation.FIELD_ACCESS_RELATION)) {
//				for (Iterator it2 = relation.getChildren().iterator(); it2.hasNext(); ) {
//					LinkNode linkNode = (LinkNode)it2.next();
//					if (linkNode.getProgramElementNode().getName().equals("this.currVal = 0")) {
//						foundNode = linkNode.getProgramElementNode();	
//					}
//				}
//			}
//		}
//		
//		assertTrue("find associated node", foundNode != null) ;
//		
//		File pointFile = createFile("testdata/examples/figures-coverage/figures/primitives/planar/Point.java");	
//		StructureNode fieldNode = Ajde.getDefault().getStructureModelManager().getStructureModel().findNodeForSourceLine(
//			pointFile.getCanonicalPath(), 12);		
//		assertTrue("find result", fieldNode != null);
//		
//		assertTrue("matches", foundNode.getParent() == fieldNode.getParent());
//	}


	public void testRootForSourceFile() throws IOException {
		File testFile = openFile("figures-coverage/figures/Figure.java");	
		StructureNode node = Ajde.getDefault().getStructureModelManager().getStructureModel().findRootNodeForSourceFile(
			testFile.getCanonicalPath());
		assertTrue("find result", node != null) ;	
		ProgramElementNode pNode = (ProgramElementNode)node;
		String child = ((StructureNode)pNode.getChildren().get(0)).getName();
        assertTrue("expected Figure got child " + child, child.equals("Figure"));
	}

	public void testPointcutName() throws IOException {
		File testFile = openFile("figures-coverage/figures/Main.java");	
		//System.err.println("PointcutName, testFile: " + testFile.getCanonicalPath() + " exists: " + testFile.exists());
		StructureNode node = Ajde.getDefault().getStructureModelManager().getStructureModel().findRootNodeForSourceFile(
			testFile.getCanonicalPath());
		//System.err.println("   node: " + node);
		assertTrue("find result", node != null) ;	
		ProgramElementNode pNode = (ProgramElementNode)((ProgramElementNode)node).getChildren().get(1);
		ProgramElementNode pointcut = (ProgramElementNode)pNode.getChildren().get(0);
		assertTrue("kind", pointcut.getProgramElementKind().equals(ProgramElementNode.Kind.POINTCUT));
		assertTrue("found node: " + pointcut.getName(), pointcut.getName().equals("testptct()"));
	}

	public void testDeclare() {
		
		
	}

	public void testFileNodeFind() throws IOException {
		File testFile = openFile("figures-coverage/figures/Main.java");
		//System.err.println("NodeFind, testFile: " + testFile.getCanonicalPath() + " exists: " + testFile.exists());
		StructureNode node = Ajde.getDefault().getStructureModelManager().getStructureModel().findNodeForSourceLine(
			testFile.getCanonicalPath(), 1);
		//System.err.println("   node: " + node);
		assertTrue("find result", node != null) ;	
		assertEquals("find result has children", 2, node.getChildren().size()) ;	
		ProgramElementNode pNode = (ProgramElementNode)node;
		assertTrue("found node: " + pNode.getName(), pNode.getProgramElementKind().equals(ProgramElementNode.Kind.FILE_JAVA));
	}
  
  	/**
  	 * @todo	add negative test to make sure things that aren't runnable aren't annotated
  	 */ 
	public void testMainClassNodeInfo() throws IOException {
        StructureModel model = Ajde.getDefault().getStructureModelManager().getStructureModel();
        assertTrue("model exists", model != null);
		assertTrue("root exists", model.getRoot() != null);
		File testFile = openFile("figures-coverage/figures/Main.java");
		StructureNode node = model.findNodeForSourceLine(testFile.getCanonicalPath(), 11);	
			
		assertTrue("find result", node != null);	
			
		ProgramElementNode pNode = (ProgramElementNode)((ProgramElementNode)node).getParent();
        if (null == pNode) {
            assertTrue("null parent of " + node, false);
        }
		assertTrue("found node: " + pNode.getName(), pNode.isRunnable());
	}  
	
	/**
	 * Integrity could be checked somewhere in the API.
	 */ 
	public void testModelIntegrity() {
		StructureNode modelRoot = Ajde.getDefault().getStructureModelManager().getStructureModel().getRoot();
		assertTrue("root exists", modelRoot != null);	
		
		try {
			testModelIntegrityHelper(modelRoot);
		} catch (Exception e) {
			assertTrue(e.toString(), false);	
		}
	}

	private void testModelIntegrityHelper(StructureNode node) throws Exception {
		for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
			StructureNode child = (StructureNode)it.next();
			if (node == child.getParent()) {
				testModelIntegrityHelper(child);
			} else {
				throw new Exception("parent-child check failed for child: " + child.toString());
			}
		}		
	}
  
  	public void testNoChildIsNull() {
  		ModelWalker walker = new ModelWalker() {
  		    public void preProcess(StructureNode node) {
  		    	if (node.getChildren() == null) return;
  		    	for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
  		    		if (it.next() == null) throw new NullPointerException("null child on node: " + node.getName());	
  		    	}
  		    }
  		};
  		Ajde.getDefault().getStructureModelManager().getStructureModel().getRoot().walk(walker);
  	}  
  
	protected void setUp() throws Exception {
		super.setUp("examples");
		doSynchronousBuild(CONFIG_FILE_PATH);	
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}

