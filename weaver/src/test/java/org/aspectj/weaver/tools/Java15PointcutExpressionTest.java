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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.internal.tools.PointcutExpressionImpl;
import org.aspectj.weaver.patterns.AbstractPatternNodeVisitor;
import org.aspectj.weaver.patterns.AndAnnotationTypePattern;
import org.aspectj.weaver.patterns.AnnotationPatternList;
import org.aspectj.weaver.patterns.AnyAnnotationTypePattern;
import org.aspectj.weaver.patterns.BindingAnnotationTypePattern;
import org.aspectj.weaver.patterns.ExactAnnotationTypePattern;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NotAnnotationTypePattern;
import org.aspectj.weaver.patterns.OrAnnotationTypePattern;
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.TypePatternList;
import org.aspectj.weaver.patterns.WildAnnotationTypePattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.A1AnnotatedType;
import test.A2AnnotatedType;

/**
 * Test parameter pointcut parsing.  Extended by Andy Clement to cover parameter annotation matching.
 *
 */
public class Java15PointcutExpressionTest extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Java15PointcutExpressionTest");
		suite.addTestSuite(Java15PointcutExpressionTest.class);
		return suite;
	}
	
	private PointcutParser parser;
	private Method a;
	private Method b;
	private Method c;
	private Method d;
	
	/**
	 * Parse some expressions and ensure we capture the parameter annotations and parameter type annotations correctly.
	 * Buckle up, this will get complicated ;)
	 */
	public void testParseParameterAnnotationExpressions() {
		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		PointcutExpression pexpr = null;

		pexpr = p.parsePointcutExpression("execution(public void foo(@MA *))");
		checkParameterAnnotations(pexpr,0,null,"@MA","exact[@MA:t]");

		pexpr = p.parsePointcutExpression("execution(public void foo(@MA (*)))");
		checkParameterAnnotations(pexpr,0,"@MA",null,"exact[@MA:p]");

		pexpr = p.parsePointcutExpression("execution(public void foo(@MA @MB *))");
		checkParameterAnnotations(pexpr,0,null,"@MA @MB","(exact[@MA:t] and exact[@MB:t])");
		
		pexpr = p.parsePointcutExpression("execution(public void foo(@MA (@MB *)))");
		checkParameterAnnotations(pexpr,0,"@MA","@MB","(exact[@MA:p] and exact[@MB:t])");
		
		pexpr = p.parsePointcutExpression("execution(public void foo(@MA @MB (@MC *)))");
		checkParameterAnnotations(pexpr,0,"@MA @MB","@MC","((exact[@MA:p] and exact[@MB:p]) and exact[@MC:t])");

		pexpr = p.parsePointcutExpression("execution(public void foo(@MA (@MB @MC @MD *)))");
		checkParameterAnnotations(pexpr,0,"@MA","@MB @MC @MD","(exact[@MA:p] and ((exact[@MB:t] and exact[@MC:t]) and exact[@MD:t]))");
				
		pexpr = p.parsePointcutExpression("execution(public void foo(@(MA || MB) (@MC @MD *)))");
		checkParameterAnnotations(pexpr,0,null/*Should be MA MB */,"@MC @MD","(wild[(MA || MB)] and (exact[@MC:t] and exact[@MD:t]))"); // I dont think WildAnnotationTypePatterns work properly...

		pexpr = p.parsePointcutExpression("execution(public void foo(@MA (@MB *),(@MC *),@MD (*)))");
		checkParameterAnnotations(pexpr,0,"@MA","@MB","(exact[@MA:p] and exact[@MB:t])");
		checkParameterAnnotations(pexpr,1,null,"@MC","exact[@MC:t]");
		checkParameterAnnotations(pexpr,2,"@MD",null,"exact[@MD:p]");
		
	}
	
	public void testMatchingAnnotationValueExpressions() throws SecurityException, NoSuchMethodException {
		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		PointcutExpression pexpr = null;
		ShadowMatch match = null;

		Method n = test.AnnoValues.class.getMethod("none");          
		Method r = test.AnnoValues.class.getMethod("redMethod");           
		Method g = test.AnnoValues.class.getMethod("greenMethod");     
		Method b = test.AnnoValues.class.getMethod("blueMethod");
		Method d = test.AnnoValues.class.getMethod("defaultMethod");

		pexpr = p.parsePointcutExpression("execution(@test.A3(test.Color.RED) public void *(..))");
		assertTrue("Should match", pexpr.matchesMethodExecution(n).neverMatches()); // default value RED
		assertTrue("Should match", pexpr.matchesMethodExecution(r).alwaysMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(g).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(b).neverMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(d).alwaysMatches());
		
		pexpr = p.parsePointcutExpression("execution(@test.A3(test.Color.GREEN) public void *(..))");
		assertTrue("Should not match", pexpr.matchesMethodExecution(n).neverMatches()); // default value RED
		assertTrue("Should not match", pexpr.matchesMethodExecution(r).neverMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(g).alwaysMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(b).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(d).neverMatches());
		
		pexpr = p.parsePointcutExpression("execution(@test.A3(test.Color.BLUE) public void *(..))");
		assertTrue("Should not match", pexpr.matchesMethodExecution(n).neverMatches()); // default value RED
		assertTrue("Should not match", pexpr.matchesMethodExecution(r).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(g).neverMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(b).alwaysMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(d).neverMatches());

		pexpr = p.parsePointcutExpression("execution(@test.A3 public void *(..))");
		assertTrue("Should match", pexpr.matchesMethodExecution(n).neverMatches()); // default value RED
		assertTrue("Should match", pexpr.matchesMethodExecution(r).alwaysMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(g).alwaysMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(b).alwaysMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(d).alwaysMatches());

	}
	
	private static final Set<PointcutPrimitive> DEFAULT_SUPPORTED_PRIMITIVES = new HashSet<>();

	 static {
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.REFERENCE);
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.THIS);
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.TARGET);
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.WITHIN);
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ANNOTATION);
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_WITHIN);
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ARGS);
	  DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_TARGET);
	 }

	public void testPerformanceOfPointcutParsing() {
		String expression = "execution(public * rewards.internal.*.*Repository+.*(..))";
		long stime1 = System.currentTimeMillis();
		PointcutParser parser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(DEFAULT_SUPPORTED_PRIMITIVES);
		long stime2 = System.currentTimeMillis();
		PointcutExpression pointcutExpression = parser.parsePointcutExpression(expression, null, new PointcutParameter[0]);
		long etime = System.currentTimeMillis();
		System.out.println("Time to get a parser "+(stime2-stime1)+"ms");
		System.out.println("Time taken to parse expression is "+(etime-stime2)+"ms");
	}
	

	public void testPerformanceOfPointcutParsingWithBean() {
		String expression = "execution(public * rewards.internal.*.*Repository+.*(..))";
		PointcutParser parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		BeanDesignatorHandler beanHandler = new BeanDesignatorHandler();
		parser.registerPointcutDesignatorHandler(beanHandler);
		long stime = System.currentTimeMillis();
		PointcutExpression pointcutExpression = parser.parsePointcutExpression(expression, null, new PointcutParameter[0]);
		long etime = System.currentTimeMillis();
		System.out.println("Time taken to parse expression is "+(etime-stime)+"ms");
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
			return null;
//			return new BeanPointcutExpression(expression,this.simulateDynamicTest);
		}
		
		public String getExpressionLastAskedToParse() {
			return this.askedToParse;
		}
	}
	

	/**
	 * Test matching of pointcuts against expressions.  A reflection world is being used on the backend here (not a Bcel one).
	 */
	public void testMatchingParameterAnnotationExpressions() throws SecurityException, NoSuchMethodException {
		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		PointcutExpression pexpr = null;
		ShadowMatch match = null;

		Method a = test.A.class.getMethod("a",new Class[] {String.class});             // public void a(String s) {}
		Method b = test.A.class.getMethod("b",new Class[] {String.class});             // public void b(@A1 String s) {}
		Method c = test.A.class.getMethod("c",new Class[] {String.class});             // public void c(@A1 @A2 String s) {}
//		Method d = test.A.class.getMethod("d",new Class[] {String.class,String.class});// public void d(@A1 String s,@A2 String t) {}

		Method e = test.A.class.getMethod("e",new Class[] {A1AnnotatedType.class});    // public void e(A1AnnotatedType s) {}
		Method f = test.A.class.getMethod("f",new Class[] {A2AnnotatedType.class});    // public void f(A2AnnotatedType s) {}
		Method g = test.A.class.getMethod("g",new Class[] {A1AnnotatedType.class});    // public void g(@A2 A1AnnotatedType s) {}
		Method h = test.A.class.getMethod("h",new Class[] {A1AnnotatedType.class});    // public void h(@A1 A1AnnotatedType s) {}
//		Method i = test.A.class.getMethod("i",new Class[] {A1AnnotatedType.class,String.class});    // public void i(A1AnnotatedType s,@A2 String t) {}
//		Method j = test.A.class.getMethod("j",new Class[] {String.class});             // public void j(@A1 @A2 String s) {}

		pexpr = p.parsePointcutExpression("execution(public void *(@test.A1 *))");
		assertTrue("Should not match", pexpr.matchesMethodExecution(a).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(b).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(c).neverMatches());
		
		pexpr = p.parsePointcutExpression("execution(public void *(@test.A1 (*)))");
		assertTrue("Should not match", pexpr.matchesMethodExecution(a).neverMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(b).alwaysMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(c).alwaysMatches());

		pexpr = p.parsePointcutExpression("execution(public void *(@test.A1 *))");
		assertTrue("Should match", pexpr.matchesMethodExecution(e).alwaysMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(f).neverMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(g).alwaysMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(h).alwaysMatches());

		pexpr = p.parsePointcutExpression("execution(public void *(@test.A1 (*)))");
		assertTrue("Should not match", pexpr.matchesMethodExecution(e).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(f).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(g).neverMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(h).alwaysMatches());

		pexpr = p.parsePointcutExpression("execution(public void *(@(test.A1 || test.A2) (*)))");
		assertTrue("Should not match", pexpr.matchesMethodExecution(a).neverMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(b).alwaysMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(c).alwaysMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(g).alwaysMatches());
		assertTrue("Should match", pexpr.matchesMethodExecution(h).alwaysMatches());

		pexpr = p.parsePointcutExpression("execution(public void *(@(test.A1 && test.A2) (*),..))");
		assertTrue("Should not match", pexpr.matchesMethodExecution(a).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(b).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(c).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(g).neverMatches());
		assertTrue("Should not match", pexpr.matchesMethodExecution(h).neverMatches());
//		assertTrue("Should match", pexpr.matchesMethodExecution(j).alwaysMatches()); // should match but does not, broken implementation, old bug - see WildAnnotationTypePattern.match

	
		
	}

	private void checkParameterAnnotations(PointcutExpression pe,int parameterNumber,String expectedParameterAnnotations,String expectedParameterTypeAnnotations,String expectedNodeStructure) {
	  org.aspectj.weaver.patterns.Pointcut p = ((PointcutExpressionImpl)pe).getUnderlyingPointcut();
	  KindedPointcut kindedP = (KindedPointcut)p;
	  SignaturePattern sp = kindedP.getSignature();
	  TypePatternList tpl = sp.getParameterTypes();
	  TypePattern[] tps = tpl.getTypePatterns();
	  
	  // A visitor over the annotation pattern for the parameter will break it down into parameter vs parameter type annotations
	  MyPatternNodeVisitor mpnv = new MyPatternNodeVisitor();
	  tps[parameterNumber].getAnnotationPattern().accept(mpnv,null);
	  
	  if (expectedNodeStructure==null) {
		  // The caller hasn't worked it out yet!!
		  System.out.println(mpnv.getStringRepresentation());
	  } else if (!mpnv.getStringRepresentation().equals(expectedNodeStructure)) {
		  System.out.println(mpnv.getStringRepresentation());
		  fail("Expected annotation pattern node structure for expression "+pe.getPointcutExpression()+
			   " was '"+expectedNodeStructure+"' but it turned out to be '"+mpnv.getStringRepresentation()+"'");
	  }
	  
	  tps[parameterNumber].getAnnotationPattern().toString();
	  
	  // parameter type annotation checking
		Set<String> expected = new HashSet<>(mpnv.getParameterTypeAnnotations());
	  
	  StringTokenizer st = new StringTokenizer(expectedParameterTypeAnnotations==null?"":expectedParameterTypeAnnotations);
	  while (st.hasMoreTokens()) {
		  String nextToken = st.nextToken();
		  if (!expected.contains(nextToken)) 
			  fail("In pointcut expression "+pe.getPointcutExpression()+" parameter "+parameterNumber+". The annotation type pattern did not include parameter type annotation "+nextToken+".  It's full set was "+mpnv.getParameterTypeAnnotations());
		  expected.remove(nextToken);
	  }
	  if (expected.size()>0) { // we have excess ones!
		  StringBuffer excessTokens = new StringBuffer();
		  for (String string : expected) {
			  excessTokens.append(string).append(" ");
		  }
	    fail("In pointcut expression "+pe.getPointcutExpression()+" parameter "+parameterNumber+". The annotation type pattern has these unexpected parameter type annotations "+excessTokens.toString());
	  }
	  
	  // parameter annotation checking
		expected = new HashSet<>(mpnv.getParameterAnnotations());
	  
	  st = new StringTokenizer(expectedParameterAnnotations==null?"":expectedParameterAnnotations);
	  while (st.hasMoreTokens()) {
		  String nextToken = st.nextToken();
		  if (!expected.contains(nextToken)) 
			  fail("In pointcut expression "+pe.getPointcutExpression()+" parameter "+parameterNumber+". The annotation type pattern did not include parameter annotation "+nextToken+".  It's full set was "+mpnv.getParameterAnnotations());
		  expected.remove(nextToken);
	  }
	  if (expected.size()>0) { // we have excess ones!
		  StringBuffer excessTokens = new StringBuffer();
		  for (String string : expected) {
			  excessTokens.append(string).append(" ");
		  }
	    fail("In pointcut expression "+pe.getPointcutExpression()+" parameter "+parameterNumber+". The annotation type pattern has these unexpected parameter annotations "+excessTokens.toString());
	  }
	  
	}
	
	static class MyPatternNodeVisitor extends AbstractPatternNodeVisitor {
		private StringBuffer stringRep = new StringBuffer();
		private List<String> parameterAnnotations = new ArrayList<>();
		private List<String> parameterTypeAnnotations = new ArrayList<>();
		
		public String getStringRepresentation() { return stringRep.toString(); }
		public List<String> getParameterAnnotations() { return parameterAnnotations; }
		public List<String> getParameterTypeAnnotations() { return parameterTypeAnnotations; }
		
		public Object visit(AndAnnotationTypePattern node, Object data) {
			stringRep.append("(");
			node.getLeft().accept(this, data);
			stringRep.append(" and ");
			node.getRight().accept(this, data);
			stringRep.append(")");
			return node;
		}
	    public Object visit(AnyAnnotationTypePattern node, Object data) {
	    	stringRep.append("any");
			return node;
	    }
	    public Object visit(ExactAnnotationTypePattern node, Object data) { 
	    	stringRep.append("exact["+stringify(node.getResolvedAnnotationType())+":"+(node.isForParameterAnnotationMatch()?"p":"t")+"]");
	    	if (node.isForParameterAnnotationMatch()) {
	    		parameterAnnotations.add(stringify(node.getResolvedAnnotationType()));
	    	} else {
	    		parameterTypeAnnotations.add(stringify(node.getResolvedAnnotationType()));
	    	}
			return node;
	    }
	    private String stringify(ResolvedType resolvedAnnotationType) {
	    	return "@"+resolvedAnnotationType.getSimpleName();
		}

		public Object visit(BindingAnnotationTypePattern node, Object data) {
			stringRep.append("binding");
	    	
			return node;
	    }
	    public Object visit(NotAnnotationTypePattern node, Object data) {
			stringRep.append("not");
			return node;
	    }
	    public Object visit(OrAnnotationTypePattern node, Object data) {
			stringRep.append("(");
			node.getLeft().accept(this, data);
			stringRep.append(" or ");
			node.getRight().accept(this, data);
			stringRep.append(")");
			return node;
	    }
	    public Object visit(WildAnnotationTypePattern node, Object data) {
			stringRep.append("wild[");
			stringRep.append(node.getTypePattern().toString());
			stringRep.append("]");
			return node;
	    }
	    public Object visit(AnnotationPatternList node, Object data) {
			stringRep.append("list");
	    	
			return node;
	    }
	    
	    
	}
	
	
	
	public void testAtThis() {
		PointcutExpression atThis = parser.parsePointcutExpression("@this(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atThis.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atThis.matchesMethodExecution(b);
		assertTrue("maybe matches A",sMatch1.maybeMatches());
		assertTrue("maybe matches B",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertFalse("does not match",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[0]);
		assertTrue("matches",jp2.matches());
	}
	
	public void testAtTarget() {
		PointcutExpression atTarget = parser.parsePointcutExpression("@target(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atTarget.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atTarget.matchesMethodExecution(b);
		assertTrue("maybe matches A",sMatch1.maybeMatches());
		assertTrue("maybe matches B",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertFalse("does not match",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[0]);
		assertTrue("matches",jp2.matches());		
	}
	
	public void testAtThisWithBinding() {
		PointcutParameter param = parser.createPointcutParameter("a",MyAnnotation.class);
		B myB = new B();
		MyAnnotation bAnnotation = B.class.getAnnotation(MyAnnotation.class);
		PointcutExpression atThis = parser.parsePointcutExpression("@this(a)",A.class,new PointcutParameter[] {param});
		ShadowMatch sMatch1 = atThis.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atThis.matchesMethodExecution(b);
		assertTrue("maybe matches A",sMatch1.maybeMatches());
		assertTrue("maybe matches B",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertFalse("does not match",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(myB, myB, new Object[0]);
		assertTrue("matches",jp2.matches());
		assertEquals(1,jp2.getParameterBindings().length);
		assertEquals("should be myB's annotation",bAnnotation,jp2.getParameterBindings()[0].getBinding());
	}
	
	public void testAtTargetWithBinding() {
		PointcutParameter param = parser.createPointcutParameter("a",MyAnnotation.class);
		B myB = new B();
		MyAnnotation bAnnotation = B.class.getAnnotation(MyAnnotation.class);
		PointcutExpression atThis = parser.parsePointcutExpression("@target(a)",A.class,new PointcutParameter[] {param});
		ShadowMatch sMatch1 = atThis.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atThis.matchesMethodExecution(b);
		assertTrue("maybe matches A",sMatch1.maybeMatches());
		assertTrue("maybe matches B",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertFalse("does not match",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(myB, myB, new Object[0]);
		assertTrue("matches",jp2.matches());
		assertEquals(1,jp2.getParameterBindings().length);
		assertEquals("should be myB's annotation",bAnnotation,jp2.getParameterBindings()[0].getBinding());
	}
	
	public void testAtArgs() {
		PointcutExpression atArgs = parser.parsePointcutExpression("@args(..,org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atArgs.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atArgs.matchesMethodExecution(c);
		assertTrue("never matches A",sMatch1.neverMatches());
		assertTrue("maybe matches C",sMatch2.maybeMatches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[]{new A(),new B()});
		assertTrue("matches",jp2.matches());	
		
		atArgs = parser.parsePointcutExpression("@args(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation,org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		sMatch1 = atArgs.matchesMethodExecution(a);
		sMatch2 = atArgs.matchesMethodExecution(c);
		assertTrue("never matches A",sMatch1.neverMatches());
		assertTrue("maybe matches C",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch2.matchesJoinPoint(new A(), new A(), new Object[] {new A(), new B()});
		assertFalse("does not match",jp1.matches());
		jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[] {new B(),new B()});
		assertTrue("matches",jp2.matches());					
	}
	
	public void testAtArgs2() {
		PointcutExpression atArgs = parser.parsePointcutExpression("@args(*, org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atArgs.matchesMethodExecution(c);
		ShadowMatch sMatch2 = atArgs.matchesMethodExecution(d);
		assertTrue("maybe matches c",sMatch1.maybeMatches());
		assertTrue("maybe matches d",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new B(), new B(), new Object[] {new A(), new B()});
		assertTrue("matches",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[] {new A(),new A()});
		assertFalse("does not match",jp2.matches());									
	}
	
	public void testAtArgsWithBinding() {
		PointcutParameter p1 = parser.createPointcutParameter("a",MyAnnotation.class);
		PointcutParameter p2 = parser.createPointcutParameter("b", MyAnnotation.class);
		PointcutExpression atArgs = parser.parsePointcutExpression("@args(..,a)",A.class,new PointcutParameter[] {p1});
		ShadowMatch sMatch2 = atArgs.matchesMethodExecution(c);
		assertTrue("maybe matches C",sMatch2.maybeMatches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[]{new A(),new B()});
		assertTrue("matches",jp2.matches());
		assertEquals(1,jp2.getParameterBindings().length);
		MyAnnotation bAnnotation = B.class.getAnnotation(MyAnnotation.class);
		assertEquals("annotation on B",bAnnotation,jp2.getParameterBindings()[0].getBinding());
		
		atArgs = parser.parsePointcutExpression("@args(a,b)",A.class,new PointcutParameter[] {p1,p2});
		sMatch2 = atArgs.matchesMethodExecution(c);
		assertTrue("maybe matches C",sMatch2.maybeMatches());
		jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[] {new B(),new B()});
		assertTrue("matches",jp2.matches());							
		assertEquals(2,jp2.getParameterBindings().length);
		assertEquals("annotation on B",bAnnotation,jp2.getParameterBindings()[0].getBinding());
		assertEquals("annotation on B",bAnnotation,jp2.getParameterBindings()[1].getBinding());		
	}
	
	public void testAtWithin() {
		PointcutExpression atWithin = parser.parsePointcutExpression("@within(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atWithin.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atWithin.matchesMethodExecution(b);
		assertTrue("does not match a",sMatch1.neverMatches());
		assertTrue("matches b",sMatch2.alwaysMatches());
	}
	
	public void testAtWithinWithBinding() {
		PointcutParameter p1 = parser.createPointcutParameter("x",MyAnnotation.class);
		PointcutExpression atWithin = parser.parsePointcutExpression("@within(x)",B.class,new PointcutParameter[] {p1});
		ShadowMatch sMatch1 = atWithin.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atWithin.matchesMethodExecution(b);
		assertTrue("does not match a",sMatch1.neverMatches());
		assertTrue("matches b",sMatch2.alwaysMatches());
		JoinPointMatch jpm = sMatch2.matchesJoinPoint(new B(), new B(), new Object[0]);
		assertTrue(jpm.matches());
		assertEquals(1,jpm.getParameterBindings().length);
		MyAnnotation bAnnotation = B.class.getAnnotation(MyAnnotation.class);
		assertEquals("annotation on B",bAnnotation,jpm.getParameterBindings()[0].getBinding());		
	}
	
	public void testAtWithinCode() {
		PointcutExpression atWithinCode = parser.parsePointcutExpression("@withincode(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atWithinCode.matchesMethodCall(a,b);
		ShadowMatch sMatch2 = atWithinCode.matchesMethodCall(a,a);
		assertTrue("does not match from b",sMatch1.neverMatches());
		assertTrue("matches from a",sMatch2.alwaysMatches());		
	}
	
	public void testAtWithinCodeWithBinding() {
		PointcutParameter p1 = parser.createPointcutParameter("x",MyAnnotation.class);
		PointcutExpression atWithinCode = parser.parsePointcutExpression("@withincode(x)",A.class,new PointcutParameter[] {p1});
		ShadowMatch sMatch2 = atWithinCode.matchesMethodCall(a,a);
		assertTrue("matches from a",sMatch2.alwaysMatches());
		JoinPointMatch jpm = sMatch2.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertEquals(1,jpm.getParameterBindings().length);
		MyAnnotation annOna = a.getAnnotation(MyAnnotation.class);
		assertEquals("MyAnnotation on a",annOna,jpm.getParameterBindings()[0].getBinding());
	}
	
	public void testAtAnnotation() {
		PointcutExpression atAnnotation = parser.parsePointcutExpression("@annotation(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atAnnotation.matchesMethodCall(b,a);
		ShadowMatch sMatch2 = atAnnotation.matchesMethodCall(a,a);
		assertTrue("does not match call to b",sMatch1.neverMatches());
		assertTrue("matches call to a",sMatch2.alwaysMatches());				
	}
	
	public void testAtAnnotationWithBinding() {
		PointcutParameter p1 = parser.createPointcutParameter("x",MyAnnotation.class);
		PointcutExpression atAnnotation = parser.parsePointcutExpression("@annotation(x)",A.class,new PointcutParameter[] {p1});
		ShadowMatch sMatch2 = atAnnotation.matchesMethodCall(a,a);
		assertTrue("matches call to a",sMatch2.alwaysMatches());				
		JoinPointMatch jpm = sMatch2.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertTrue(jpm.matches());
		assertEquals(1,jpm.getParameterBindings().length);
		MyAnnotation annOna = a.getAnnotation(MyAnnotation.class);
		assertEquals("MyAnnotation on a",annOna,jpm.getParameterBindings()[0].getBinding());		
	}
	
	public void testReferencePointcutNoParams() {
		PointcutExpression pc = parser.parsePointcutExpression("foo()",C.class,new PointcutParameter[0]);
		ShadowMatch sMatch1 = pc.matchesMethodCall(a,b);
		ShadowMatch sMatch2 = pc.matchesMethodExecution(a);
		assertTrue("no match on call",sMatch1.neverMatches());
		assertTrue("match on execution",sMatch2.alwaysMatches());
		
		pc = parser.parsePointcutExpression("org.aspectj.weaver.tools.Java15PointcutExpressionTest.C.foo()");
		sMatch1 = pc.matchesMethodCall(a,b);
		sMatch2 = pc.matchesMethodExecution(a);
		assertTrue("no match on call",sMatch1.neverMatches());
		assertTrue("match on execution",sMatch2.alwaysMatches());
	}
	
	public void testReferencePointcutParams() {
		PointcutParameter p1 = parser.createPointcutParameter("x",A.class);
		PointcutExpression pc = parser.parsePointcutExpression("goo(x)",C.class,new PointcutParameter[] {p1});

		ShadowMatch sMatch1 = pc.matchesMethodCall(a,b);
		ShadowMatch sMatch2 = pc.matchesMethodExecution(a);
		assertTrue("no match on call",sMatch1.neverMatches());
		assertTrue("match on execution",sMatch2.maybeMatches());
		A anA = new A();
		JoinPointMatch jpm = sMatch2.matchesJoinPoint(anA, new A(), new Object[0]);
		assertTrue(jpm.matches());
		assertEquals("should be bound to anA",anA,jpm.getParameterBindings()[0].getBinding());

	}
	
	public void testExecutionWithClassFileRetentionAnnotation() {
		PointcutExpression pc1 = parser.parsePointcutExpression("execution(@org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation * *(..))");
		PointcutExpression pc2 = parser.parsePointcutExpression("execution(@org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyClassFileRetentionAnnotation * *(..))");
		ShadowMatch sMatch = pc1.matchesMethodExecution(a);
		assertTrue("matches",sMatch.alwaysMatches());
		sMatch = pc2.matchesMethodExecution(a);
		assertTrue("no match",sMatch.neverMatches());
		sMatch = pc1.matchesMethodExecution(b);
		assertTrue("no match",sMatch.neverMatches());
		sMatch = pc2.matchesMethodExecution(b);
		assertTrue("matches",sMatch.alwaysMatches());
	}
	
	public void testGenericMethodSignatures() throws Exception{
		PointcutExpression ex = parser.parsePointcutExpression("execution(* set*(java.util.List<org.aspectj.weaver.tools.Java15PointcutExpressionTest.C>))");
		Method m = TestBean.class.getMethod("setFriends",List.class);
		ShadowMatch sm = ex.matchesMethodExecution(m);
		assertTrue("should match",sm.alwaysMatches());		
	}
	
	public void testAnnotationInExecution() throws Exception {
		parser.parsePointcutExpression("execution(@(org.springframework..*) * *(..))");		
	}
	
	public void testVarArgsMatching() throws Exception {
		PointcutExpression ex = parser.parsePointcutExpression("execution(* *(String...))");
		Method usesVarArgs = D.class.getMethod("varArgs",String[].class);
		Method noVarArgs = D.class.getMethod("nonVarArgs", String[].class);
		ShadowMatch sm1 = ex.matchesMethodExecution(usesVarArgs);
		assertTrue("should match",sm1.alwaysMatches());				
		ShadowMatch sm2 = ex.matchesMethodExecution(noVarArgs);
		assertFalse("should not match",sm2.alwaysMatches());				
	}
	
	public void testJavaLangMatching() throws Exception {
		PointcutExpression ex = parser.parsePointcutExpression("@within(java.lang.Deprecated)");
		Method foo = GoldenOldie.class.getMethod("foo");
		ShadowMatch sm1 = ex.matchesMethodExecution(foo);
		assertTrue("should match",sm1.alwaysMatches());
	}
	
	public void testReferencePCsInSameType() throws Exception {
		PointcutExpression ex = parser.parsePointcutExpression("org.aspectj.weaver.tools.Java15PointcutExpressionTest.NamedPointcutResolution.c()",NamedPointcutResolution.class,new PointcutParameter[0]);
		ShadowMatch sm = ex.matchesMethodExecution(a);
		assertTrue("should match",sm.alwaysMatches());
		sm = ex.matchesMethodExecution(b);
		assertTrue("does not match",sm.neverMatches());
	}
	
	public void testReferencePCsInOtherType() throws Exception {
		PointcutExpression ex = parser.parsePointcutExpression("org.aspectj.weaver.tools.Java15PointcutExpressionTest.ExternalReferrer.d()",ExternalReferrer.class,new PointcutParameter[0]);
		ShadowMatch sm = ex.matchesMethodExecution(a);
		assertTrue("should match",sm.alwaysMatches());
		sm = ex.matchesMethodExecution(b);
		assertTrue("does not match",sm.neverMatches());		
	}
	
	public void testArrayTypeInArgs() throws Exception {
		PointcutParameter[] params = new PointcutParameter[3];
		params[0] = parser.createPointcutParameter("d", Date.class);
		params[1] = parser.createPointcutParameter("s", String.class);
		params[2] = parser.createPointcutParameter("ss", String[].class);
		parser.parsePointcutExpression("org.aspectj.weaver.tools.Java15PointcutExpressionTest.UsesArrays.pc(d,s,ss)",UsesArrays.class,params);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		a = A.class.getMethod("a");
		b = B.class.getMethod("b");
		c = B.class.getMethod("c",new Class[] {A.class,B.class});
		d = B.class.getMethod("d",new Class[] {A.class,A.class});
	}

	@Retention(RetentionPolicy.RUNTIME)
	private @interface MyAnnotation {}
	
	private @interface MyClassFileRetentionAnnotation {}
	
	private static class A {
		@MyAnnotation public void a() {}
	}
	
	@MyAnnotation
	private static class B {
		@MyClassFileRetentionAnnotation public void b() {}
		public void c(A anA, B aB) {}
		
		public void d(A anA, A anotherA) {}
	}
	
	private static class C {
		
		@Pointcut("execution(* *(..))")
		public void foo() {}
		
		@Pointcut(value="execution(* *(..)) && this(x)", argNames="x")
		public void goo(A x) {}
	}
	
	private static class D {
		
		public void nonVarArgs(String[] strings) {};
		
		public void varArgs(String... strings) {};
		
	}
	
	static class TestBean {
		public void setFriends(List<C> friends) {}
	}

	@Deprecated
	static class GoldenOldie {
		public void foo() {}
	}
	
	private static class NamedPointcutResolution {
		
		@Pointcut("execution(* *(..))")
		public void a() {}
		
		@Pointcut("this(org.aspectj.weaver.tools.Java15PointcutExpressionTest.A)")
		public void b() {}
		
		@Pointcut("a() && b()")
		public void c() {}
	}
	
	private static class ExternalReferrer {
		
	  @Pointcut("org.aspectj.weaver.tools.Java15PointcutExpressionTest.NamedPointcutResolution.a() && " + 
			    "org.aspectj.weaver.tools.Java15PointcutExpressionTest.NamedPointcutResolution.b())")
	  public void d() {}
		
	}
	
	private static class UsesArrays {
		
		@Pointcut("execution(* *(..)) && args(d,s,ss)")
		public void pc(Date d, String s, String[] ss) {}
		
	}
}


