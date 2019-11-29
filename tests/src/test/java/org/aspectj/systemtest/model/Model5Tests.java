/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.systemtest.model;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.World;

import junit.framework.Test;

/**
 * Tests the model when there is a requirement on Java5 features.
 * 
 * @see org.aspectj.systemtest.model.ModelTestCase
 */
public class Model5Tests extends ModelTestCase {

	static {
		// Switch this to true for a single iteration if you want to reconstruct the
		// 'expected model' files.
		regenerate = false;
		// Switch this to true if you want to debug the comparison
		debugTest = false;
	}

	public void testDeclareAtTypeInStructureModel_pr115607() {
		runModelTest("declare at type appears correctly in structure model", "pr115607");
	}

	public void testStructureModelForGenericITD_pr131932() {
		runModelTest("structure model for generic itd", "pr131932");
	}

	public void testDeclareAnnotationAppearsInStructureModel_pr132130() {
		runModelTest("declare annotation appears in structure model when in same file", "pr132130");
	}

	public void testAtAspectDEOWInStructureModel_pr120356() {
		runModelTest("@AJ deow appear correctly when structure model is generated", "pr120356");
	}

	public void testDeclareAtMethodRelationship_pr143924() {
		runModelTest("declare @method relationship", "pr143924");
	}

	public void testNewIProgramElementMethodsForGenerics_pr141730() {
		runModelTest("new iprogramelement methods for generics", "pr141730_2");
	}

	// if not filling in the model for classes contained in jar files then
	// want to ensure that the relationship map is correct and has nodes
	// which can be used in AJDT - ensure no NPE occurs for the end of
	// the relationship with inpath
	public void testAspectPathRelWhenNotFillingInModel_pr141730() {
		World.createInjarHierarchy = false;
		try {
			// the aspect used for this test has advice, declare parents, deow,
			// and declare @type, @constructor, @field and @method. We only expect
			// there to be relationships in the map for declare parents and declare @type
			// (provided the model isn't being filled in) because the logic in the other
			// addXXXRelationship methods use AspectJElementHierarchy.findElementForType().
			// This method which returns null because there is no such ipe. The relationship is
			// therefore not added to the model. Adding declare @type and declare parents
			// uses AspectJElementHierarchy.findElementForHandle() which returns the file
			// node ipe if it can't find one for the given handle. Therefore the relationships
			// are added against the file node. Before change to using ipe's to create handles
			// we also had the deow relationship, however, now we don't because this also
			// uses findElementForType to find the targetNode which in the inpath case is null.
			runModelTest("ensure inpath injar relationships are correct when not filling in model", "pr141730_4");
		} finally {
			World.createInjarHierarchy = true;
		}
	}

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Model5Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("model.xml");
	}

}
