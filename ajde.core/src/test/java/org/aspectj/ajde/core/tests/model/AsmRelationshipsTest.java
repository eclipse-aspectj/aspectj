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
import java.util.List;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;

public class AsmRelationshipsTest extends AjdeCoreTestCase {

	private AsmManager manager = null;

	private final String[] files = new String[] { "ModelCoverage.java", "pkg" + File.separator + "InPackage.java" };

	private TestCompilerConfiguration compilerConfig;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("coverage");
		compilerConfig = (TestCompilerConfiguration) getCompiler().getCompilerConfiguration();
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		doBuild();
		manager = AsmManager.lastActiveStructureModel;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		compilerConfig = null;
		manager = null;
	}

	// // see pr148027
	// public void testUsesPointcut() {
	// if (!AsmHierarchyBuilder.shouldAddUsesPointcut) return;
	//
	// IProgramElement ptUsage = AsmManager.getDefault().getHierarchy().findElementForType(null, "PointcutUsage");
	// assertNotNull(ptUsage);
	// IProgramElement pts = AsmManager.getDefault().getHierarchy().findElementForType(null, "Pointcuts");
	// assertNotNull(pts);
	//
	// IProgramElement pUsesA = manager.getHierarchy().findElementForLabel(
	// ptUsage,
	// IProgramElement.Kind.POINTCUT,
	// "usesA()"/*Point"*/);
	// assertNotNull(pUsesA);
	//
	// IProgramElement ptsA = manager.getHierarchy().findElementForLabel(
	// pts,
	// IProgramElement.Kind.POINTCUT,
	// "a()"/*Point"*/);
	// assertNotNull(ptsA);
	//
	// assertTrue(AsmManager.getDefault().getRelationshipMap().get(pUsesA).size()>0);
	// assertTrue(AsmManager.getDefault().getRelationshipMap().get(ptsA).size()>0);
	// }

	public void testDeclareParents() {
		IProgramElement aspect = manager.getHierarchy().findElementForType(null, "DeclareCoverage");

		IProgramElement dp = manager.getHierarchy().findElementForLabel(aspect, IProgramElement.Kind.DECLARE_PARENTS,
				"declare parents: implements Serializable"/* Point" */);

		assertNotNull(dp);
		/* List relations = */manager.getRelationshipMap().get(dp);

		List<IRelationship> rels = manager.getRelationshipMap().get(dp);
		assertTrue(rels.size() > 0);

		// assertTrue(rel.getTargets().size() > 0);
		//
		// checkDeclareMapping("DeclareCoverage", "Point", ,
		// "Point", "matched by", "matches declare",
		// IProgramElement.Kind.DECLARE_PARENTS);
	}

	public void testDeclareWarningAndError() {
		checkDeclareMapping("DeclareCoverage", "Point", "declare warning: \"Illegal call.\"", "method-call(void Point.setX(int))",
				"matched by", "matches declare", IProgramElement.Kind.DECLARE_WARNING);
	}

	public void testInterTypeDeclarations() {
		checkInterTypeMapping("InterTypeDecCoverage", "Point", "Point.xxx", "Point", "declared on", "aspect declarations",
				IProgramElement.Kind.INTER_TYPE_FIELD);
		checkInterTypeMapping("InterTypeDecCoverage", "Point", "Point.check(int,Line)", "Point", "declared on",
				"aspect declarations", IProgramElement.Kind.INTER_TYPE_METHOD);
	}

	public void testAdvice() {
		checkMapping("AdvisesRelationshipCoverage", "Point", "before(): methodExecutionP..", "setX(int)", "advises", "advised by");
		checkUniDirectionalMapping("AdvisesRelationshipCoverage", "Point", "before(): getP..", "field-get(int Point.x)", "advises");
		checkUniDirectionalMapping("AdvisesRelationshipCoverage", "Point", "before(): setP..", "field-set(int Point.x)", "advises");
	}

	private void checkDeclareMapping(String fromType, String toType, String from, String to, String forwardRelName,
			String backRelName, IProgramElement.Kind kind) {

		IProgramElement aspect = manager.getHierarchy().findElementForType(null, fromType);
		assertNotNull(aspect);
		String beforeExec = from;
		IProgramElement beforeExecNode = manager.getHierarchy().findElementForLabel(aspect, kind, beforeExec);
		assertNotNull(beforeExecNode);
		IRelationship rel = manager.getRelationshipMap().get(beforeExecNode, IRelationship.Kind.DECLARE, forwardRelName);
		assertTrue(rel.getTargets().size() > 0);
		String handle = rel.getTargets().get(0);
		assertEquals(manager.getHierarchy().findElementForHandle(handle).toString(), to);

		IProgramElement clazz = manager.getHierarchy().findElementForType(null, toType);
		assertNotNull(clazz);
		String set = to;
		IProgramElement setNode = manager.getHierarchy().findElementForLabel(clazz, IProgramElement.Kind.CODE, set);
		assertNotNull(setNode);
		IRelationship rel2 = manager.getRelationshipMap().get(setNode, IRelationship.Kind.DECLARE, backRelName);
		String handle2 = rel2.getTargets().get(0);
		assertEquals(manager.getHierarchy().findElementForHandle(handle2).toString(), from);
	}

	private void checkUniDirectionalMapping(String fromType, String toType, String from, String to, String relName) {

		IProgramElement aspect = manager.getHierarchy().findElementForType(null, fromType);
		assertNotNull(aspect);
		String beforeExec = from;
		IProgramElement beforeExecNode = manager.getHierarchy()
				.findElementForLabel(aspect, IProgramElement.Kind.ADVICE, beforeExec);
		assertNotNull(beforeExecNode);
		IRelationship rel = manager.getRelationshipMap().get(beforeExecNode, IRelationship.Kind.ADVICE, relName);
		for (String currHandle : rel.getTargets()) {
			if (manager.getHierarchy().findElementForHandle(currHandle).toLabelString().equals(to))
				return;
		}
		fail(); // didn't find it
	}

	private void checkMapping(String fromType, String toType, String from, String to, String forwardRelName, String backRelName) {

		IProgramElement aspect = manager.getHierarchy().findElementForType(null, fromType);
		assertNotNull(aspect);
		String beforeExec = from;
		IProgramElement beforeExecNode = manager.getHierarchy()
				.findElementForLabel(aspect, IProgramElement.Kind.ADVICE, beforeExec);
		assertNotNull(beforeExecNode);
		IRelationship rel = manager.getRelationshipMap().get(beforeExecNode, IRelationship.Kind.ADVICE, forwardRelName);
		String handle = rel.getTargets().get(0);
		assertEquals(manager.getHierarchy().findElementForHandle(handle).toString(), to);

		IProgramElement clazz = manager.getHierarchy().findElementForType(null, toType);
		assertNotNull(clazz);
		String set = to;
		IProgramElement setNode = manager.getHierarchy().findElementForLabel(clazz, IProgramElement.Kind.METHOD, set);
		assertNotNull(setNode);
		IRelationship rel2 = manager.getRelationshipMap().get(setNode, IRelationship.Kind.ADVICE, backRelName);
		String handle2 = rel2.getTargets().get(0);
		assertEquals(manager.getHierarchy().findElementForHandle(handle2).toString(), from);
	}

	private void checkInterTypeMapping(String fromType, String toType, String from, String to, String forwardRelName,
			String backRelName, IProgramElement.Kind declareKind) {

		IProgramElement aspect = manager.getHierarchy().findElementForType(null, fromType);
		assertNotNull(aspect);
		String beforeExec = from;
		IProgramElement fromNode = manager.getHierarchy().findElementForLabel(aspect, declareKind, beforeExec);
		assertNotNull(fromNode);
		IRelationship rel = manager.getRelationshipMap().get(fromNode, IRelationship.Kind.DECLARE_INTER_TYPE, forwardRelName);
		String handle = rel.getTargets().get(0);
		assertEquals(manager.getHierarchy().findElementForHandle(handle).toString(), to);

		IProgramElement clazz = manager.getHierarchy().findElementForType(null, toType);
		assertNotNull(clazz);
		// String set = to;
		IRelationship rel2 = manager.getRelationshipMap().get(clazz, IRelationship.Kind.DECLARE_INTER_TYPE, backRelName);
		// String handle2 = (String)rel2.getTargets().get(0);
		for (String currHandle : rel2.getTargets()) {
			if (manager.getHierarchy().findElementForHandle(currHandle).toLabelString().equals(from))
				return;
		}
		fail(); // didn't find it
	}
}
