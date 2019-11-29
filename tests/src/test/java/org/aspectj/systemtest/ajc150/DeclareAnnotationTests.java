/*******************************************************************************
 * Copyright (c) 2005 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement   initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class DeclareAnnotationTests extends XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(DeclareAnnotationTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc150.xml");
	}

	// parsing the various forms of declare @
	public void testDeclareAnnotationParsing() {
		runTest("basic declare annotation parse test");
	}

	// declare @type

	// declare @type for one simple annotation on one specific type
	public void testAtType_OneAnnotationHittingOneType_Src() {
		runTest("declare @type 1");
	}

	// declare @type for one simple annotation to multiple types
	public void testAtType_OneAnnotationHittingMultipleTypes_Src() {
		runTest("declare @type 2");
	}

	// declare @type for one simple annotation and a pointcut that matches on it
	public void testAtType_PointcutMatchingOnDeclaredAnnotation() {
		runTest("declare @type - with matching pointcut");
	}

	// binary weaving declare @type, one annotation on one type
	public void testAtType_OneAnnotationHittingOneType_Bin() {
		runTest("declare @type - binary weaving");
	}

	// an annotation with multiple values (all the primitives and string)
	// is declared upon a single type
	public void testAtType_ComplexAnnotation_BinWeaving() {
		runTest("declare @type - complex annotation - binary weaving");
	}

	public void testAtType_ComplexAnnotation_SrcWeaving() {
		runTest("declare @type - complex annotation - source weaving");
	}

	// two annotations are declared on a type
	public void testAtType_TwoAnnotationsOnOneType_BinWeaving() {
		runTest("declare @type - two annotations hit one type - binary weaving");
	}

	public void testAtType_TwoAnnotationsOnOneType_SrcWeaving() {
		runTest("declare @type - two annotations hit one type - source weaving");
	}

	// decp and deca can interact, let's try some variants that should
	// result in the same thing
	public void testAtType_InteractingWithDeclareParents1_BinWeaving() {
		runTest("declare @type - declare parents interactions (order 1) - binary weaving");
	}

	public void testAtType_InteractingWithDeclareParents1_SrcWeaving() {
		runTest("declare @type - declare parents interactions (order 1) - source weaving");
	}

	public void testAtType_InteractingWithDeclareParents2_BinWeaving() {
		runTest("declare @type - declare parents interactions (order 2) - binary weaving");
	}

	public void testAtType_InteractingWithDeclareParents2_SrcWeaving() {
		runTest("declare @type - declare parents interactions (order 2) - source weaving");
	}

	public void testAtType_InteractingWithDeclareParents3_BinWeaving() {
		runTest("declare @type - declare parents interactions (order 3) - binary weaving");
	}

	public void testAtType_InteractingWithDeclareParents3_SrcWeaving() {
		runTest("declare @type - declare parents interactions (order 3) - source weaving");
	}

	public void testAtType_InteractingWithDeclareParents4_BinWeaving() {
		runTest("declare @type - declare parents interactions (order 4) - binary weaving");
	}

	public void testAtType_InteractingWithDeclareParents4_SrcWeaving() {
		runTest("declare @type - declare parents interactions (order 4) - source weaving");
	}

	public void testAtType_AnnotatingAlreadyAnnotatedType_BinWeaving() {
		runTest("declare @type - annotating an already annotated type - binary weaving");
	}

	public void testAtType_AnnotatingAlreadyAnnotatedType_SrcWeaving() {
		runTest("declare @type - annotating an already annotated type - source weaving");
	}

	// testing for error messages when exact type patterns used
	// public void testAtType_UsingWrongAnnotationOnAType_BinWeaving()
	// runTest("declare @type - annotations with different targets - binary weaving");
	// }
	public void testAtType_UsingWrongAnnotationOnAType_SrcWeaving() {
		runTest("declare @type - annotations with different targets - source weaving");
	}

	// testing for the lint message when non exact patterns used
	// public void testAtType_UsingWrongAnnotationOnAType_TypeSpecifiedByPattern_BinWeaving() {
	// runTest("declare @type - annotations with different targets (using type patterns) - binary weaving");
	// }
	public void testAtType_UsingWrongAnnotationOnAType_TypeSpecifiedByPattern_SrcWeaving() {
		runTest("declare @type - annotations with different targets (using type patterns) - source weaving");
	}

	// testing how multiple decAtType/decps interact when they rely on each other
	public void testAtType_ComplexDecpDecAtTypeInteractions_BinWeaving() {
		runTest("declare @type - complex decp decAtType interactions - binary weaving");
	}

	public void testAtType_ComplexDecpDecAtTypeInteractions_SrcWeaving() {
		runTest("declare @type - complex decp decAtType interactions - source weaving");
	}

	public void testAtType_PuttingIncorrectAnnosOnTypes_SrcWeaving() {
		runTest("declare @type - trying to put annotation targetting annos on normal types - source weaving");
	}

	public void testAtType_PuttingIncorrectAnnosOnTypes_BinWeaving() {
		runTest("declare @type - trying to put annotation targetting annos on normal types - binary weaving");
	}

	public void testAtType_PuttingIncorrectAnnosOnTypesWithPatterns_SrcWeaving() {
		runTest("declare @type - trying to put annotation targetting annos on normal types (uses pattern) - source weaving");
	}

	public void testAtType_PuttingIncorrectAnnosOnTypesWithPatterns_BinWeaving() {
		runTest("declare @type - trying to put annotation targetting annos on normal types (uses pattern) - binary weaving");
	}

	// I think this fails because of a freaky JDT compiler bug ...
	// public void testAtType_UsingClassOrEnumElementValuesInAnnotations_SrcWeaving() {
	// runTest("declare @type - covering enum and class element values - source weaving");
	// }

	public void testAtType_UsingClassOrEnumElementValuesInAnnotations_BinWeaving() {
		runTest("declare @type - covering enum and class element values - binary weaving");
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// declare @field

	public void testAtField_SimpleSource() {
		runTest("declare @field - simple source weaving");
	}

	public void testAtField_SimpleBinary() {
		runTest("declare @field - simple binary weaving");
	}

	// lint warning
	public void testAtField_TwoTheSameOnOneSource() {
		runTest("declare @field - two the same on one - source weaving");
	}

	// lint warning
	public void testAtField_TwoTheSameOnOneBinary() {
		runTest("declare @field - two the same on one - binary weaving");
	}

	public void testAtField_TwoDifferentOnOneSource() {
		runTest("declare @field - two different on one - source weaving");
	}

	public void testAtField_TwoDifferentOnOneBinary() {
		runTest("declare @field - two different on one - binary weaving");
	}

	public void testAtField_WrongTargetSource() {
		runTest("declare @field - wrong target - source weaving");
	}

	// Can't do a test like this - as verification of the declare @ is
	// done when the aspect is first compiled.
	// public void testAtField_WrongTargetBinary() {
	// runTest("declare @field - wrong target - binary weaving");
	// }

	public void testAtField_RightTargetSource() {
		runTest("declare @field - right target - source weaving");
	}

	public void testAtField_RightTargetBinary() {
		runTest("declare @field - right target - binary weaving");
	}

	public void testAtField_RecursiveSource() {
		runTest("declare @field - recursive application - source weaving");
	}

	public void testAtField_RecursiveBinary() {
		runTest("declare @field - recursive application - binary weaving");
	}

	public void testAtField_RecursiveOtherOrderSource() {
		runTest("declare @field - recursive application (other order) - source weaving");
	}

	public void testAtField_RecursiveOtherOrderBinary() {
		runTest("declare @field - recursive application (other order) - binary weaving");
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// declare @method

	public void testAtMethod_SimpleSource() {
		runTest("declare @method - simple source weaving");
	}

	public void testAtMethod_SimpleBinary() {
		runTest("declare @method - simple binary weaving");
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// declare @constructor

	public void testAtCtor_SimpleSource() {
		runTest("declare @constructor - simple source weaving");
	}

	public void testAtCtor_SimpleBinary() {
		runTest("declare @constructor - simple binary weaving");
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// declare @method @constructor

	public void testAtMethodCtor_WrongTargetSource() {
		runTest("declare @method @ctor - wrong target - source weaving");
	}

	public void testAtMethodCtor_RightTargetSource() {
		runTest("declare @method @ctor - right target - source weaving");
	}

	public void testAtMethodCtor_RightTargetBinary() {
		runTest("declare @method @ctor - right target - binary weaving");
	}

	// lint warning
	public void testAtMethodCtor_TwoTheSameOnOneSource() {
		runTest("declare @method @ctor - two the same on one - source weaving");
	}

	// lint warning
	public void testAtMethodCtor_TwoTheSameOnOneBinary() {
		runTest("declare @method @ctor - two the same on one - binary weaving");
	}

	public void testAtMethodCtor_TwoDifferentOnOneSource() {
		runTest("declare @method @ctor - two different on one - source weaving");
	}

	public void testAtMethodCtor_TwoDifferentOnOneBinary() {
		runTest("declare @method @ctor - two different on one - binary weaving");
	}

	// to debug this test, uncomment the first line which will give you a nice
	// dump of the structure model in c:/debug.txt
	public void testStructureModel() {
		// AsmManager.setReporting("c:/debug.txt",true,true,true,true);
		runTest("declare all annotations on one class - source weaving");

		if (getCurrentTest().canRunOnThisVM()) {
			IHierarchy top = AsmManager.lastActiveStructureModel.getHierarchy();

			IProgramElement ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_TYPE,
					"declare @type: p.q.DeathByAnnotations : @Colored(\"red\")");
			assertTrue("Couldn't find 'declare @type' element in the tree", ipe != null);

			List<IRelationship> l = AsmManager.lastActiveStructureModel.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l != null && l.size() > 0);

			ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_METHOD,
					"declare @method: * m*(..) : @Fruit(\"tomato\")");
			assertTrue("Couldn't find 'declare @method element in the tree", ipe != null);

			l = AsmManager.lastActiveStructureModel.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l.size() > 0);

			ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_CONSTRUCTOR,
					"declare @constructor: p.q.DeathByAnnotations.new(..) : @Fruit(\"tomato\")");
			assertTrue("Couldn't find 'declare @constructor element in the tree", ipe != null);
			l = AsmManager.lastActiveStructureModel.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l.size() > 0);

			ipe = top.findElementForLabel(top.getRoot(), IProgramElement.Kind.DECLARE_ANNOTATION_AT_FIELD,
					"declare @field: * p.q.DeathByAnnotations.* : @Material(\"wood\")");
			assertTrue("Couldn't find 'declare @field element in the tree", ipe != null);
			l = AsmManager.lastActiveStructureModel.getRelationshipMap().get(ipe);
			assertTrue("Should have a relationship but does not ", l.size() > 0);
		}
	}

	public void testDeclareTypeMisspelled() {
		runTest("declare @Type (should be @type)");
	}

}