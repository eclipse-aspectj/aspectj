/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * ******************************************************************/
package org.aspectj.weaver.tools;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test cases for the PointcutParser class
 */
public class PointcutParserTest extends TestCase {

	private boolean needToSkip = false;

	/** this condition can occur on the build machine only, and is way too complex to fix right now... */
	private boolean needToSkipPointcutParserTests() {
		try {
			Class.forName("org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate", false, this.getClass()
					.getClassLoader());// ReflectionBasedReferenceTypeDelegate.class.getClassLoader());
		} catch (ClassNotFoundException cnfEx) {
			return true;
		}
		return false;
	}

	protected void setUp() throws Exception {
		super.setUp();
		needToSkip = needToSkipPointcutParserTests();
	}

	public void testGetAllSupportedPointcutPrimitives() {
		if (needToSkip) {
			return;
		}

		Set<PointcutPrimitive> s = PointcutParser.getAllSupportedPointcutPrimitives();
		assertEquals("Should be 21 elements in the set", 21, s.size());
		assertFalse("Should not contain if pcd", s.contains(PointcutPrimitive.IF));
		assertFalse("Should not contain cflow pcd", s.contains(PointcutPrimitive.CFLOW));
		assertFalse("Should not contain cflowbelow pcd", s.contains(PointcutPrimitive.CFLOW_BELOW));
	}

	public void testEmptyConstructor() {
		if (needToSkip) {
			return;
		}

		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		Set<PointcutPrimitive> s = parser.getSupportedPrimitives();
		assertEquals("Should be 21 elements in the set", 21, s.size());
		assertFalse("Should not contain if pcd", s.contains(PointcutPrimitive.IF));
		assertFalse("Should not contain cflow pcd", s.contains(PointcutPrimitive.CFLOW));
		assertFalse("Should not contain cflowbelow pcd", s.contains(PointcutPrimitive.CFLOW_BELOW));
	}

	public void testSetConstructor() {
		if (needToSkip) {
			return;
		}

		Set<PointcutPrimitive> p = PointcutParser.getAllSupportedPointcutPrimitives();
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(p, this.getClass()
						.getClassLoader());
		assertEquals("Should use the set we pass in", p, parser.getSupportedPrimitives());
		Set<PointcutPrimitive> q = new HashSet<>();
		q.add(PointcutPrimitive.ARGS);
		parser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(q, this
				.getClass().getClassLoader());
		assertEquals("Should have only one element in set", 1, parser.getSupportedPrimitives().size());
		assertEquals("Should only have ARGS pcd", PointcutPrimitive.ARGS, parser.getSupportedPrimitives().iterator().next());
	}

	public void testParsePointcutExpression() {
		if (needToSkip) {
			return;
		}

		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this
				.getClass().getClassLoader());
		IMessageHandler current = p.setCustomMessageHandler(new IgnoreWarningsMessageHandler());
		try {
			p.parsePointcutExpression("(adviceexecution() || execution(* *.*(..)) || handler(Exception) || "
					+ "call(Foo Bar+.*(Goo)) || get(* foo) || set(Foo+ (Goo||Moo).s*) || "
					+ "initialization(Foo.new(..)) || preinitialization(*.new(Foo,..)) || "
					+ "staticinitialization(org.xzy.abc..*)) && (this(Foo) || target(Boo) ||" + "args(A,B,C)) && !handler(X)");
		} finally {
			p.setCustomMessageHandler(current);
		}
		try {
			p.parsePointcutExpression("gobble-de-gook()");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
		}
	}

	public void testParseExceptionErrorMessages() {
		if (needToSkip) {
			return;
		}

		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this
				.getClass().getClassLoader());
		try {
			p.parsePointcutExpression("execution(int Foo.*(..) && args(Double)");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			assertTrue("Pointcut is not well-formed message",
					ex.getMessage().startsWith("Pointcut is not well-formed: expecting ')' at character position 24"));
		}
	}

	public void testOperatorPrecedence_319190() throws Exception {
		if (needToSkip) {
			return;
		}

		String s = null;
		Pointcut p = null;

		s = "(execution(* A.credit(float)) || execution(* A.debit(float))) && this(acc) && args(am)  || execution(* C.*(Account, float)) && args(acc, am)";
		p = new PatternParser(s).parsePointcut();
		Assert.assertEquals(
				"(((execution(* A.credit(float)) || execution(* A.debit(float))) && (this(acc) && args(am))) || (execution(* C.*(Account, float)) && args(acc, am)))",
				p.toString());

		s = "(if(true) || if(false)) && this(acc) && args(am)  || if(true) && args(acc, am)";
		p = new PatternParser(s).parsePointcut();
		// bugged was: ((if(true) || if(false)) && (this(acc) && (args(am) || (if(true) && args(acc, am)))))
		Assert.assertEquals("(((if(true) || if(false)) && (this(acc) && args(am))) || (if(true) && args(acc, am)))", p.toString());
		p = new PointcutRewriter().rewrite(p);
		Assert.assertEquals("(((this(acc) && args(am)) && if(true)) || (args(acc, am) && if(true)))", p.toString());

		s = "if(true) && if(false) || if(true)";
		p = new PatternParser(s).parsePointcut();
		assertEquals("((if(true) && if(false)) || if(true))", p.toString());
		p = new PointcutRewriter().rewrite(p);
		Assert.assertEquals("if(true)", p.toString());
	}

	public void testParseIfPCD() {
		if (needToSkip) {
			return;
		}

		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this
				.getClass().getClassLoader());
		try {
			p.parsePointcutExpression("if(true)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Should not support IF", PointcutPrimitive.IF, ex.getUnsupportedPrimitive());
		}
	}

	public void testParseCflowPCDs() {
		if (needToSkip) {
			return;
		}

		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this
				.getClass().getClassLoader());
		try {
			p.parsePointcutExpression("cflow(this(t))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Should not support CFLOW", PointcutPrimitive.CFLOW, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("cflowbelow(this(t))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Should not support CFLOW_BELOW", PointcutPrimitive.CFLOW_BELOW, ex.getUnsupportedPrimitive());
		}
	}

	public void testParseReferencePCDs() {
		if (needToSkip) {
			return;
		}

		Set<PointcutPrimitive> pcKinds = PointcutParser.getAllSupportedPointcutPrimitives();
		pcKinds.remove(PointcutPrimitive.REFERENCE);
		PointcutParser p = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(
				pcKinds, this.getClass().getClassLoader());
		try {
			p.parsePointcutExpression("bananas(String)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertTrue(ex.getUnsupportedPrimitive() == PointcutPrimitive.REFERENCE);
		}
	}

	public void testParseUnsupportedPCDs() {
		if (needToSkip) {
			return;
		}

		Set s = new HashSet();
		PointcutParser p = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(
				s, this.getClass().getClassLoader());
		try {
			p.parsePointcutExpression("args(x)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Args", PointcutPrimitive.ARGS, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("within(x)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Within", PointcutPrimitive.WITHIN, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("withincode(new(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Withincode", PointcutPrimitive.WITHIN_CODE, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("handler(Exception)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("handler", PointcutPrimitive.HANDLER, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("this(X)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("this", PointcutPrimitive.THIS, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("target(X)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("target", PointcutPrimitive.TARGET, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("this(X) && target(Y)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("This", PointcutPrimitive.THIS, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("this(X) || target(Y)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("This", PointcutPrimitive.THIS, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("!this(X)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("This", PointcutPrimitive.THIS, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("call(* *.*(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Call", PointcutPrimitive.CALL, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("execution(* *.*(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Execution", PointcutPrimitive.EXECUTION, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("get(* *)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Get", PointcutPrimitive.GET, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("set(* *)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Set", PointcutPrimitive.SET, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("initialization(new(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Initialization", PointcutPrimitive.INITIALIZATION, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("preinitialization(new(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Prc-init", PointcutPrimitive.PRE_INITIALIZATION, ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("staticinitialization(T)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch (UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Staticinit", PointcutPrimitive.STATIC_INITIALIZATION, ex.getUnsupportedPrimitive());
		}
	}

	public void testFormals() {
		if (needToSkip) {
			return;
		}

		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		PointcutParameter param = parser.createPointcutParameter("x", String.class);
		PointcutExpression pc = parser.parsePointcutExpression("args(x)", null, new PointcutParameter[] { param });
		assertEquals("args(x)", pc.getPointcutExpression());

		try {
			pc = parser.parsePointcutExpression("args(String)", null, new PointcutParameter[] { param });
			fail("Expecting IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			assertTrue("formal unbound", ex.getMessage().contains("formal unbound"));
		}

		try {
			pc = parser.parsePointcutExpression("args(y)");
			fail("Expecting IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			assertTrue("no match for type name", ex.getMessage().contains("warning no match for this type name: y"));
		}
	}

	public void testXLintConfiguration() {
		if (needToSkip) {
			return;
		}

		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this
				.getClass().getClassLoader());
		try {
			p.parsePointcutExpression("this(FooBar)");
		} catch (IllegalArgumentException ex) {
			assertTrue("should have xlint:invalidAbsoluteTypeName", ex.getMessage().contains("Xlint:invalidAbsoluteTypeName"));
		}
		Properties props = new Properties();
		props.put("invalidAbsoluteTypeName", "ignore");
		p.setLintProperties(props);
		p.parsePointcutExpression("this(FooBar)");
	}

	private static class IgnoreWarningsMessageHandler implements IMessageHandler {

		public boolean handleMessage(IMessage message) throws AbortException {
			if (message.getKind() != IMessage.WARNING) {
				throw new RuntimeException("unexpected message: " + message.toString());
			}
			return true;
		}

		public boolean isIgnoring(Kind kind) {
			if (kind != IMessage.ERROR) {
				return true;
			}
			return false;
		}

		public void dontIgnore(Kind kind) {
		}

		public void ignore(Kind kind) {
		}

	}
}
