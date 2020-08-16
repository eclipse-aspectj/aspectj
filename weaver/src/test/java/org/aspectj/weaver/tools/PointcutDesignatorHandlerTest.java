/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.tools;

import junit.framework.TestCase;

/**
 * @author Adrian Colyer
 * 
 */
public class PointcutDesignatorHandlerTest extends TestCase {

	boolean needToSkip = false;

	protected void setUp() throws Exception {
		super.setUp();
		needToSkip = needToSkipPointcutParserTests();
	}
	
	/** this condition can occur on the build machine only, and is way too complex to fix right now... */
	private boolean needToSkipPointcutParserTests() {
		try {
			Class.forName("org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate",false,this.getClass().getClassLoader());//ReflectionBasedReferenceTypeDelegate.class.getClassLoader()); 
		} catch (ClassNotFoundException cnfEx) {
			return true;
		}
		return false;
	}
	
	public void testParseWithoutHandler() {
		if (needToSkip) return;
		try {
			PointcutParser
			  .getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution()
			  .parsePointcutExpression("bean(service.*");
			fail("should not be able to parse bean(service.*)");
		} catch(IllegalArgumentException ex) {
			assertTrue("contains bean", ex.getMessage().contains("bean"));
		}
	}
	
	public void testParseWithHandler() {
		if (needToSkip) return;
		PointcutParser parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
		BeanDesignatorHandler beanHandler = new BeanDesignatorHandler();
		parser.registerPointcutDesignatorHandler(beanHandler);
		parser.parsePointcutExpression("bean(service.*)");
		assertEquals("service.*",beanHandler.getExpressionLastAskedToParse());
	}
	
    
	/*
     * Bug 205907 - the registered pointcut designator does not also get registered with the
     * InternalUseOnlyPointcutParser inside the Java15ReflectionBasedReferenceTypeDelegate code. First test checks
     * parsing is OK
     */
    public void testParsingBeanInReferencePointcut01() throws Exception {
        if (needToSkip) return;
        PointcutParser parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
        BeanDesignatorHandler beanHandler = new BeanDesignatorHandler();
        parser.registerPointcutDesignatorHandler(beanHandler);
        // The pointcut in CounterAspect look as follows:
        //
        // @Pointcut("execution(* setAge(..)) && bean(testBean1)")
        // public void testBean1SetAge() { }

        // This should be found and resolved
//        PointcutExpression pc = 
        	parser.parsePointcutExpression("CounterAspect.testBean1SetAge()");

    }

    /*
     * Bug 205907 - the registered pointcut designator does not also get registered with the
     * InternalUseOnlyPointcutParser inside the Java15ReflectionBasedReferenceTypeDelegate code. This test checks the
     * actual matching.
     */
    public void testParsingBeanInReferencePointcut02() throws Exception {
        if (needToSkip) return;
        PointcutParser parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
        BeanDesignatorHandler beanHandler = new BeanDesignatorHandler();
        parser.registerPointcutDesignatorHandler(beanHandler);
        // The pointcut in CounterAspect look as follows:
        //
        // @Pointcut("execution(* toString(..)) && bean(testBean1)")
        // public void testBean1toString() { }
        
        // This should be found and resolved
        PointcutExpression pc = parser.parsePointcutExpression("CounterAspect.testBean1toString()");

        DefaultMatchingContext context = new DefaultMatchingContext();
        context.addContextBinding("beanName", "testBean1");
        pc.setMatchingContext(context);
        ShadowMatch sm = pc.matchesMethodExecution(Object.class.getMethod("toString", new Class[0]));
        assertTrue(sm.alwaysMatches());
        
        sm = pc.matchesMethodExecution(Object.class.getMethod("hashCode", new Class[0]));
        assertTrue(sm.neverMatches());
        
        context = new DefaultMatchingContext();
        context.addContextBinding("beanName", "testBean2");
        pc.setMatchingContext(context);
        sm = pc.matchesMethodExecution(Object.class.getMethod("toString", new Class[0]));
        assertTrue(sm.neverMatches());
    }

    public void testParseWithHandlerAndMultipleSegments() {
        if (needToSkip) return;
        PointcutParser parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
		BeanDesignatorHandler beanHandler = new BeanDesignatorHandler();
		parser.registerPointcutDesignatorHandler(beanHandler);
		parser.parsePointcutExpression("bean(org.xyz.someapp..*)");
		assertEquals("org.xyz.someapp..*",beanHandler.getExpressionLastAskedToParse());	
	}
	
	public void testStaticMatch() throws Exception {
		if (needToSkip) return;
		PointcutParser parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
		BeanDesignatorHandler beanHandler = new BeanDesignatorHandler();
		parser.registerPointcutDesignatorHandler(beanHandler);
		PointcutExpression pc = parser.parsePointcutExpression("bean(myBean)");
		DefaultMatchingContext context = new DefaultMatchingContext();
		context.addContextBinding("beanName","myBean");
		pc.setMatchingContext(context);
		ShadowMatch sm = pc.matchesMethodExecution(Object.class.getMethod("toString",new Class[0]));
		assertTrue(sm.alwaysMatches());
		context.addContextBinding("beanName", "notMyBean");
		sm = pc.matchesMethodExecution(Object.class.getMethod("toString",new Class[0]));
		assertTrue(sm.neverMatches());
	}
	
	public void testDynamicMatch() throws Exception {
		if (needToSkip) return;
		PointcutParser parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
		BeanDesignatorHandler beanHandler = new BeanDesignatorHandler();
		beanHandler.simulateDynamicTest = true;
		parser.registerPointcutDesignatorHandler(beanHandler);
		PointcutExpression pc = parser.parsePointcutExpression("bean(myBean)");
		ShadowMatch sm = pc.matchesMethodExecution(Object.class.getMethod("toString",new Class[0]));
		DefaultMatchingContext context = new DefaultMatchingContext();
		assertTrue(sm.maybeMatches());
		assertFalse(sm.alwaysMatches());
		assertFalse(sm.neverMatches());
		context.addContextBinding("beanName","myBean");
		sm.setMatchingContext(context);
		assertTrue(sm.matchesJoinPoint(null, null, null).matches());
		context.addContextBinding("beanName", "notMyBean");
		assertFalse(sm.matchesJoinPoint(null, null, null).matches());
	}
	
	public void testFastMatch() {
		if (needToSkip) return;
		PointcutParser parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
		BeanDesignatorHandler beanHandler = new BeanDesignatorHandler();
		parser.registerPointcutDesignatorHandler(beanHandler);
		PointcutExpression pc = parser.parsePointcutExpression("bean(myBean)");
		DefaultMatchingContext context = new DefaultMatchingContext();
		context.addContextBinding("beanName","myBean");
		pc.setMatchingContext(context);
		assertTrue(pc.couldMatchJoinPointsInType(String.class));
		context.addContextBinding("beanName","yourBean");
		assertFalse(pc.couldMatchJoinPointsInType(String.class));		
	}

	private class BeanDesignatorHandler implements PointcutDesignatorHandler {

		private String askedToParse;
		public boolean simulateDynamicTest = false;
		
		public String getDesignatorName() {
			return "bean";
		}
	
		/* (non-Javadoc)
		 * @see org.aspectj.weaver.tools.PointcutDesignatorHandler#parse(java.lang.String)
		 */
		public ContextBasedMatcher parse(String expression) {
			this.askedToParse = expression;
			return new BeanPointcutExpression(expression,this.simulateDynamicTest);
		}
		
		public String getExpressionLastAskedToParse() {
			return this.askedToParse;
		}
	}
	
	private class BeanPointcutExpression implements ContextBasedMatcher {

		private final String beanNamePattern;
		private final boolean simulateDynamicTest;

		public BeanPointcutExpression(String beanNamePattern, boolean simulateDynamicTest) {
			this.beanNamePattern = beanNamePattern;
			this.simulateDynamicTest = simulateDynamicTest;			
		}


		public boolean couldMatchJoinPointsInType(Class aClass) {
			return true;
		}
		
		/* (non-Javadoc)
		 * @see org.aspectj.weaver.tools.ContextBasedMatcher#couldMatchJoinPointsInType(java.lang.Class)
		 */
		public boolean couldMatchJoinPointsInType(Class aClass, MatchingContext context) {
			if (this.beanNamePattern.equals(context.getBinding("beanName"))) {
				return true;
			} else {
				return false;
			}
		}


		/* (non-Javadoc)
		 * @see org.aspectj.weaver.tools.ContextBasedMatcher#mayNeedDynamicTest()
		 */
		public boolean mayNeedDynamicTest() {
			return this.simulateDynamicTest;
		}


		public FuzzyBoolean matchesStatically(MatchingContext matchContext) {
			if (this.simulateDynamicTest) return FuzzyBoolean.MAYBE;
			if (this.beanNamePattern.equals(matchContext.getBinding("beanName"))) {
				return FuzzyBoolean.YES;
			} else {
				return FuzzyBoolean.NO;
			}
		}


		/* (non-Javadoc)
		 * @see org.aspectj.weaver.tools.ContextBasedMatcher#matchesDynamically(org.aspectj.weaver.tools.MatchingContext)
		 */
		public boolean matchesDynamically(MatchingContext matchContext) {
			return this.beanNamePattern.equals(matchContext.getBinding("beanName"));
		}
	}		
}
