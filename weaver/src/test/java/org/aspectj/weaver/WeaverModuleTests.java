/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

import org.aspectj.matcher.tools.ReflectionWorldAdvancedPointcutExpressionTest;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.bcel.AfterReturningWeaveTestCase;
import org.aspectj.weaver.bcel.AfterThrowingWeaveTestCase;
import org.aspectj.weaver.bcel.AfterWeaveTestCase;
import org.aspectj.weaver.bcel.ArgsWeaveTestCase;
import org.aspectj.weaver.bcel.AroundArgsWeaveTestCase;
import org.aspectj.weaver.bcel.AroundWeaveTestCase;
import org.aspectj.weaver.bcel.BcelGenericSignatureToTypeXTestCase;
import org.aspectj.weaver.bcel.BcelWorldReferenceTypeTest;
import org.aspectj.weaver.bcel.BeforeWeaveTestCase;
import org.aspectj.weaver.bcel.CheckerTestCase;
import org.aspectj.weaver.bcel.ClassLoaderRepositoryTest;
import org.aspectj.weaver.bcel.FieldSetTestCase;
import org.aspectj.weaver.bcel.HierarchyDependsTestCase;
import org.aspectj.weaver.bcel.IdWeaveTestCase;
import org.aspectj.weaver.bcel.JImageTestCase;
import org.aspectj.weaver.bcel.MegaZipTestCase;
import org.aspectj.weaver.bcel.MoveInstructionsWeaveTestCase;
import org.aspectj.weaver.bcel.NonstaticWeaveTestCase;
import org.aspectj.weaver.bcel.PatternWeaveTestCase;
import org.aspectj.weaver.bcel.PointcutResidueTestCase;
import org.aspectj.weaver.bcel.TjpWeaveTestCase;
import org.aspectj.weaver.bcel.TraceJarWeaveTestCase;
import org.aspectj.weaver.bcel.UtilityTestCase;
import org.aspectj.weaver.bcel.WeaveOrderTestCase;
import org.aspectj.weaver.bcel.WorldTestCase;
import org.aspectj.weaver.bcel.ZipTestCase;
import org.aspectj.weaver.patterns.AnnotationPatternMatchingTestCase;
import org.aspectj.weaver.patterns.AnnotationPatternTestCase;
import org.aspectj.weaver.patterns.ConcretizationTestCase;
import org.aspectj.weaver.patterns.WildTypePatternResolutionTestCase;
import org.aspectj.weaver.patterns.bcel.BcelAndOrNotTestCase;
import org.aspectj.weaver.patterns.bcel.BcelBindingTestCase;
import org.aspectj.weaver.patterns.bcel.BcelModifiersPatternTestCase;
import org.aspectj.weaver.patterns.bcel.BcelParserTestCase;
import org.aspectj.weaver.patterns.bcel.BcelSignaturePatternTestCase;
import org.aspectj.weaver.patterns.bcel.BcelTypePatternListTestCase;
import org.aspectj.weaver.patterns.bcel.BcelTypePatternTestCase;
import org.aspectj.weaver.patterns.bcel.BcelWithinTestCase;
import org.aspectj.weaver.reflect.ReflectionWorldReferenceTypeTest;
import org.aspectj.weaver.reflect.ReflectionWorldTest;
import org.aspectj.weaver.tools.Java15PointcutExpressionTest;
import org.aspectj.weaver.tools.PointcutDesignatorHandlerTest;
import org.aspectj.weaver.tools.PointcutExpressionTest;
import org.aspectj.weaver.tools.PointcutParserTest;
import org.aspectj.weaver.tools.ReadingAttributesTest;
import org.aspectj.weaver.tools.TypePatternMatcherTest;
import org.aspectj.weaver.tools.cache.DefaultCacheKeyResolverTest;
import org.aspectj.weaver.tools.cache.DefaultFileCacheBackingTest;
import org.aspectj.weaver.tools.cache.FlatFileCacheBackingTest;
import org.aspectj.weaver.tools.cache.SimpleClassCacheTest;
import org.aspectj.weaver.tools.cache.WeavedClassCacheTest;
import org.aspectj.weaver.tools.cache.ZippedFileCacheBackingTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class WeaverModuleTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(WeaverModuleTests.class.getName());
		suite.addTestSuite(BoundedReferenceTypeTestCase.class);
		suite.addTestSuite(CommonsTraceFactoryTest.class);
		suite.addTestSuite(CommonsTraceTest.class);
		suite.addTestSuite(DefaultTraceFactoryTest.class);
		suite.addTestSuite(DefaultTraceTest.class);
		suite.addTestSuite(DumpTestCase.class);
		suite.addTestSuite(GenericSignatureParserTest.class);
		suite.addTestSuite(Java5ReflectionBasedReferenceTypeDelegateTest.class);
		suite.addTestSuite(Jdk14TraceFactoryTest.class);
		suite.addTestSuite(Jdk14TraceTest.class);
		suite.addTestSuite(JoinPointSignatureIteratorTest.class);
		suite.addTestSuite(LocaleTest.class);
		suite.addTestSuite(Member15Test.class);
		suite.addTestSuite(Member15TestCase.class);
		suite.addTestSuite(MemberTestCase.class);
		suite.addTestSuite(ParameterizedReferenceTypeTestCase.class);
		suite.addTestSuite(ReferenceTypeTestCase.class);
		suite.addTestSuite(ResolvedMemberSignatures15TestCase.class);
		suite.addTestSuite(TraceFactoryTest.class);
		suite.addTestSuite(TypeVariableTestCase.class);
		suite.addTestSuite(WeaverMessagesTestCase.class);

		suite.addTestSuite(AfterReturningWeaveTestCase.class);
		suite.addTestSuite(AfterThrowingWeaveTestCase.class);
		suite.addTestSuite(AfterWeaveTestCase.class);
		suite.addTestSuite(ArgsWeaveTestCase.class);
		suite.addTestSuite(AroundArgsWeaveTestCase.class);
		suite.addTestSuite(AroundWeaveTestCase.class);
		suite.addTestSuite(BcelGenericSignatureToTypeXTestCase.class);
		suite.addTestSuite(BcelWorldReferenceTypeTest.class);
		suite.addTestSuite(BeforeWeaveTestCase.class);
		suite.addTestSuite(CheckerTestCase.class);
		suite.addTestSuite(ClassLoaderRepositoryTest.class);
		suite.addTestSuite(FieldSetTestCase.class);
		suite.addTestSuite(HierarchyDependsTestCase.class);
		suite.addTestSuite(IdWeaveTestCase.class);
        if (LangUtil.is19VMOrGreater()) {
        	suite.addTestSuite(JImageTestCase.class);
        }
		suite.addTestSuite(MegaZipTestCase.class);
		suite.addTestSuite(MoveInstructionsWeaveTestCase.class);
		suite.addTestSuite(NonstaticWeaveTestCase.class);
		suite.addTestSuite(PatternWeaveTestCase.class);
		suite.addTestSuite(PointcutResidueTestCase.class);
		suite.addTestSuite(TjpWeaveTestCase.class);
		suite.addTestSuite(TraceJarWeaveTestCase.class);
		suite.addTestSuite(UtilityTestCase.class);
		suite.addTestSuite(WeaveOrderTestCase.class);
		suite.addTestSuite(WorldTestCase.class);
		suite.addTestSuite(ZipTestCase.class);
		suite.addTestSuite(TypeXTestCase.class);
		
		suite.addTestSuite(AnnotationPatternMatchingTestCase.class);
		suite.addTestSuite(AnnotationPatternTestCase.class);
		suite.addTestSuite(ConcretizationTestCase.class);
		suite.addTestSuite(WildTypePatternResolutionTestCase.class);

		suite.addTestSuite(BcelAndOrNotTestCase.class);
		suite.addTestSuite(BcelBindingTestCase.class);
		suite.addTestSuite(BcelModifiersPatternTestCase.class);
		suite.addTestSuite(BcelParserTestCase.class);
		suite.addTestSuite(BcelSignaturePatternTestCase.class);
		suite.addTestSuite(BcelTypePatternListTestCase.class);
		suite.addTestSuite(BcelTypePatternTestCase.class);
		suite.addTestSuite(BcelWithinTestCase.class);

		suite.addTestSuite(ReflectionWorldReferenceTypeTest.class);
		suite.addTestSuite(ReflectionWorldTest.class);

		suite.addTestSuite(Java15PointcutExpressionTest.class);
		suite.addTestSuite(PointcutDesignatorHandlerTest.class);
		suite.addTestSuite(PointcutExpressionTest.class);
		suite.addTestSuite(PointcutParserTest.class);
		suite.addTestSuite(ReadingAttributesTest.class);
		suite.addTestSuite(TypePatternMatcherTest.class);

		suite.addTestSuite(DefaultCacheKeyResolverTest.class);
		suite.addTestSuite(DefaultFileCacheBackingTest.class);
		suite.addTestSuite(FlatFileCacheBackingTest.class);
		suite.addTestSuite(SimpleClassCacheTest.class);
		suite.addTestSuite(WeavedClassCacheTest.class);
		suite.addTestSuite(ZippedFileCacheBackingTest.class);

		suite.addTestSuite(ReflectionWorldAdvancedPointcutExpressionTest.class);
		suite.addTestSuite(TypeVariableReferenceTypeTestCase.class);

		return suite;
	}

	public WeaverModuleTests(String name) {
		super(name);
	}

}
