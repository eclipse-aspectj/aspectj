/*******************************************************************************
 * Copyright (c) 2008-2012 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc170;

import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.TypeFactory;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.internal.tools.StandardPointcutExpressionImpl;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;
import org.aspectj.weaver.reflect.ReflectionWorld;
import org.aspectj.weaver.tools.StandardPointcutParser;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc170Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

//	public void testLostAnnos_377130() {
//		runTest("missing annos on priv aspects");
//	}
//
//	public void testLostAnnos_377130_2() {
//		runTest("missing annos on priv aspects - 2");
//	}

	public void testCovariantGenerics382435_1() {
		runTest("covariant generic itds 1");
	}

	public void testCovariantGenerics382435_2() {
		runTest("covariant generic itds 2");
	}

	public void testCovariantGenericsItd382189_1() {
		runTest("covariant generics 1");
	}

	public void testCovariantGenericsItd382189_2() {
		runTest("covariant generics 2");
	}

	public void testCovariantGenericsItd382189_3() {
		runTest("covariant generics 3");
	}

	public void testCovariantGenericsItd382189() {
		runTest("covariant generics");
	}

	public void testGenericAspectAround382723() {
		runTest("generic aspect");
	}

	public void testGenericAspectAround382723_2() {
		runTest("generic aspect 2");
	}

	public void testGenericAspectAround382723_3() {
		runTest("generic aspect 3");
	}

	public void testGenericAspectAround382723_4() {
		runTest("generic aspect 4");
	}


	public void testAttributeErrorJ7() {
		runTest("attribute issue with J7");
	}

	public void testSwitchOnEnum() {
		runTest("switch on enum");
	}

	public void testDecAtFieldOrderingLTW1() {
		runTest("dec at field ordering ltw 1");
	}

	public void testDecAtFieldOrdering1() {
		runTest("dec at field ordering 1");
	}

//	public void testDecAtFieldOrdering2() {
//		runTest("dec at field ordering 2");
//	}

	public void testXmlDefsDeclareAnnoType() {
		runTest("xml defined dec anno - type");
	}

	public void testXmlDefsDeclareAnnoMethod() {
		runTest("xml defined dec at method");
	}

	// anno not runtime vis
	public void testXmlDefsDeclareAnnoMethod2() {
		runTest("xml defined dec at method 2");
	}

	public void testXmlDefsDeclareAnnoField() {
		runTest("xml defined dec at field");
	}

	public void testXmlDefsDeclareAnnoFieldVariants1() {
		runTest("xml defined dec anno - variants 1");
	}

	public void testXmlDefsDeclareAnnoFieldVariants2() {
		runTest("xml defined dec anno - variants 2");
	}

	public void testXmlDefsDeclareAnnoFieldMultipleValues() {
		runTest("xml defined dec anno - multiple values");
	}

	public void testXmlDefsDeclareAnnoFieldMultipleValuesAndSpaces() {
		runTest("xml defined dec anno - multiple values and spaces");
	}

	public void testPointcutExpense_374964() {
		// check a declaring type being specified causes the call() to be considered cheaper than this()

		World world = new ReflectionWorld(true, getClass().getClassLoader());
		StandardPointcutParser pointcutParser = StandardPointcutParser.getPointcutParserSupportingAllPrimitives(world);
		StandardPointcutExpressionImpl pointcutExpression = (StandardPointcutExpressionImpl)pointcutParser.parsePointcutExpression("call(* *(..)) && this(Object)");
		Pointcut pc = pointcutExpression.getUnderlyingPointcut();
		Pointcut newp = new PointcutRewriter().rewrite(pc);
		// no declaring type so this() is considered cheaper
		assertEquals("(this(java.lang.Object) && call(* *(..)))",newp.toString());

		pointcutExpression = (StandardPointcutExpressionImpl)pointcutParser.parsePointcutExpression("call(* String.*(..)) && this(Object)");
		pc = pointcutExpression.getUnderlyingPointcut();
		newp = new PointcutRewriter().rewrite(pc);
		// declaring type, so call() is cheaper
		assertEquals("(call(* java.lang.String.*(..)) && this(java.lang.Object))",newp.toString());

		pointcutExpression = (StandardPointcutExpressionImpl)pointcutParser.parsePointcutExpression("this(Object) && call(* *(..)) && call(* String.*(..))");
		pc = pointcutExpression.getUnderlyingPointcut();
		newp = new PointcutRewriter().rewrite(pc);
		// more complex example, mix of them
		assertEquals("((call(* java.lang.String.*(..)) && this(java.lang.Object)) && call(* *(..)))",newp.toString());
	}

	public void testBCExceptionAnnoDecp_371998() {
		runTest("BCException anno decp");
    }

	public void testTransientTjpFields()throws Exception {
		runTest("transient tjp fields");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "Code");
		Field[] fs = jc.getFields();
		//private static final org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_0 [Synthetic]
		//private static final org.aspectj.lang.JoinPoint$StaticPart ajc$tjp_1 [Synthetic]
		for (Field f: fs) {
			if (!f.isTransient()) {
				fail("Field should be transient: "+f);
			}
		}
	}

	public void testGenericsWithTwoTypeParamsOneWildcard() {
		UnresolvedType ut;

		ut = TypeFactory.createTypeFromSignature("LFoo<**>;");
		assertEquals(2,ut.getTypeParameters().length);

		ut = TypeFactory.createTypeFromSignature("LFoo<***>;");
		assertEquals(3,ut.getTypeParameters().length);

		ut = TypeFactory.createTypeFromSignature("LFoo<TP;*+Ljava/lang/String;>;");
		assertEquals(2,ut.getTypeParameters().length);

		ut = TypeFactory.createTypeFromSignature("LFoo<*+Ljava/lang/String;TP;>;");
		assertEquals(2,ut.getTypeParameters().length);

		ut = TypeFactory.createTypeFromSignature("LFoo<*+Ljava/lang/String;TP;>;");
		assertEquals(2,ut.getTypeParameters().length);

		ut = TypeFactory.createTypeFromSignature("LFoo<*TT;>;");
		assertEquals(2,ut.getTypeParameters().length);

		ut = TypeFactory.createTypeFromSignature("LFoo<[I>;");
		assertEquals(1,ut.getTypeParameters().length);

		ut = TypeFactory.createTypeFromSignature("LFoo<[I[Z>;");
		assertEquals(2,ut.getTypeParameters().length);
	}

	public void testPerThis() {
		runTest("perthis");
	}

	public void testPerTarget() {
		runTest("pertarget");
	}

	public void testPerCflow() {
		runTest("percflow");
	}

	public void testPerTypeWithin() {
		runTest("pertypewithin");
	}

	// not specifying -1.7
	public void testDiamond1() {
		runTest("diamond 1");
	}

	public void testDiamond2() {
		runTest("diamond 2");
	}

	public void testDiamondItd1() {
		runTest("diamond itd 1");
	}

	public void testLiterals1() {
		runTest("literals 1");
	}

	public void testLiterals2() {
		runTest("literals 2");
	}

	public void testLiteralsItd1() {
		runTest("literals itd 1");
	}

	public void testStringSwitch1() {
		runTest("string switch 1");
	}

	public void testStringSwitch2() {
		runTest("string switch 2");
	}

	public void testMultiCatch1() {
		runTest("multi catch 1");
	}

	public void testMultiCatch2() {
		runTest("multi catch 2");
	}

	public void testMultiCatchWithHandler1() {
		runTest("multi catch with handler 1");
	}

	public void testMultiCatchAspect1() {
		runTest("multi catch aspect 1");
	}

	// public void testMultiCatchWithHandler2() {
	// runTest("multi catch with handler 2");
	// }

	public void testSanity1() {
		runTest("sanity 1");
	}

	public void testMissingImpl_363979() {
		runTest("missing impl");
	}

	public void testMissingImpl_363979_2() {
		runTest("missing impl 2");
	}

	public void testStackOverflow_364380() {
		runTest("stackoverflow");
	}

	// public void testTryResources1() {
	// runTest("try resources 1");
	// }
	//
	// public void testTryResources2() {
	// runTest("try resources 2");
	// }

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc170Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
        return getClassResource("ajc170.xml");
	}

}
