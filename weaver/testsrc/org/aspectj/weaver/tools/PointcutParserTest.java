/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.tools;

import java.util.HashSet;
import java.util.Set;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessage.Kind;

import junit.framework.TestCase;

/**
 * Test cases for the PointcutParser class
 */
public class PointcutParserTest extends TestCase {

	public void testGetAllSupportedPointcutPrimitives() {
		Set s = PointcutParser.getAllSupportedPointcutPrimitives();
		assertEquals("Should be 21 elements in the set",21,s.size());
		assertFalse("Should not contain if pcd",s.contains(PointcutPrimitive.IF));
		assertFalse("Should not contain cflow pcd",s.contains(PointcutPrimitive.CFLOW));
		assertFalse("Should not contain cflowbelow pcd",s.contains(PointcutPrimitive.CFLOW_BELOW));
	}
	
	public void testEmptyConstructor() {
		PointcutParser parser = new PointcutParser();
		Set s = parser.getSupportedPrimitives();
		assertEquals("Should be 21 elements in the set",21,s.size());
		assertFalse("Should not contain if pcd",s.contains(PointcutPrimitive.IF));
		assertFalse("Should not contain cflow pcd",s.contains(PointcutPrimitive.CFLOW));
		assertFalse("Should not contain cflowbelow pcd",s.contains(PointcutPrimitive.CFLOW_BELOW));
	}
	
	public void testSetConstructor() {
		Set p = PointcutParser.getAllSupportedPointcutPrimitives();
		PointcutParser parser = new PointcutParser(p);
		assertEquals("Should use the set we pass in",p,parser.getSupportedPrimitives());
		Set q = new HashSet();
		q.add(PointcutPrimitive.ARGS);
		parser = new PointcutParser(q);
		assertEquals("Should have only one element in set",1,parser.getSupportedPrimitives().size());
		assertEquals("Should only have ARGS pcd",PointcutPrimitive.ARGS,
				parser.getSupportedPrimitives().iterator().next());
	}
	
	public void testParsePointcutExpression() {
		PointcutParser p = new PointcutParser();
		IMessageHandler current = p.setCustomMessageHandler(new IgnoreWarningsMessageHandler());
		try {			
			p.parsePointcutExpression(
					"(adviceexecution() || execution(* *.*(..)) || handler(Exception) || " +
					"call(Foo Bar+.*(Goo)) || get(* foo) || set(Foo+ (Goo||Moo).s*) || " +
					"initialization(Foo.new(..)) || preinitialization(*.new(Foo,..)) || " +
					"staticinitialization(org.xzy.abc..*)) && (this(Foo) || target(Boo) ||" +
					"args(A,B,C)) && !handler(X)");
		} finally {
			p.setCustomMessageHandler(current);
		}
		try {
			p.parsePointcutExpression("gobble-de-gook()");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException ex) {}
	}
	
	public void testParseExceptionErrorMessages() {
		PointcutParser p = new PointcutParser();
		try {
			p.parsePointcutExpression("execution(int Foo.*(..) && args(Double)");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			assertTrue("Pointcut is not well-formed message",ex.getMessage().startsWith("Pointcut is not well-formed: expecting ')' at character position 24"));
		}		
	}
	
	public void testParseIfPCD() {
		PointcutParser p = new PointcutParser();
		try {
			p.parsePointcutExpression("if(true)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Should not support IF",PointcutPrimitive.IF,ex.getUnsupportedPrimitive());
		}
	}
	
	public void testParseCflowPCDs() {
		PointcutParser p = new PointcutParser();
		try {
			p.parsePointcutExpression("cflow(this(t))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Should not support CFLOW",PointcutPrimitive.CFLOW,ex.getUnsupportedPrimitive());
		}
		try {
			p.parsePointcutExpression("cflowbelow(this(t))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Should not support CFLOW_BELOW",PointcutPrimitive.CFLOW_BELOW,ex.getUnsupportedPrimitive());
		}	
	}
	
	public void testParseReferencePCDs() {
		Set pcKinds = PointcutParser.getAllSupportedPointcutPrimitives();
		pcKinds.remove(PointcutPrimitive.REFERENCE);
		PointcutParser p = new PointcutParser(pcKinds);
		try {
			p.parsePointcutExpression("bananas(String)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertTrue(ex.getUnsupportedPrimitive() == PointcutPrimitive.REFERENCE);
		}	
	}

	public void testParseUnsupportedPCDs() {
		Set s = new HashSet();
		PointcutParser p = new PointcutParser(s);
		try {
			p.parsePointcutExpression("args(x)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Args",PointcutPrimitive.ARGS,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("within(x)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Within",PointcutPrimitive.WITHIN,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("withincode(new(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Withincode",PointcutPrimitive.WITHIN_CODE,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("handler(Exception)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("handler",PointcutPrimitive.HANDLER,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("this(X)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("this",PointcutPrimitive.THIS,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("target(X)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("target",PointcutPrimitive.TARGET,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("this(X) && target(Y)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("This",PointcutPrimitive.THIS,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("this(X) || target(Y)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("This",PointcutPrimitive.THIS,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("!this(X)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("This",PointcutPrimitive.THIS,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("call(* *.*(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Call",PointcutPrimitive.CALL,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("execution(* *.*(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Execution",PointcutPrimitive.EXECUTION,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("get(* *)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Get",PointcutPrimitive.GET,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("set(* *)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Set",PointcutPrimitive.SET,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("initialization(new(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Initialization",PointcutPrimitive.INITIALIZATION,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("preinitialization(new(..))");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Prc-init",PointcutPrimitive.PRE_INITIALIZATION,ex.getUnsupportedPrimitive());
		}	
		try {
			p.parsePointcutExpression("staticinitialization(T)");
			fail("Expected UnsupportedPointcutPrimitiveException");
		} catch(UnsupportedPointcutPrimitiveException ex) {
			assertEquals("Staticinit",PointcutPrimitive.STATIC_INITIALIZATION,ex.getUnsupportedPrimitive());
		}	
	}	
	
	public void testFormals() {
		PointcutParser parser = new PointcutParser();
		PointcutParameter param = parser.createPointcutParameter("x",String.class);
		PointcutExpression pc = parser.parsePointcutExpression("args(x)", null, new PointcutParameter[] {param} );
		assertEquals("args(x)",pc.getPointcutExpression());
		
		try {
			pc = parser.parsePointcutExpression("args(String)", null, new PointcutParameter[] {param} );
			fail("Expecting IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			assertTrue("formal unbound",ex.getMessage().indexOf("formal unbound") != -1);
		}
		
		try {
			pc = parser.parsePointcutExpression("args(y)");
			fail("Expecting IllegalArgumentException");
		} catch(IllegalArgumentException ex) {
			assertTrue("no match for type name",ex.getMessage().indexOf("warning no match for this type name: y") != -1);
		}
	}
	
	private static class IgnoreWarningsMessageHandler implements IMessageHandler {

		public boolean handleMessage(IMessage message) throws AbortException {
			if (message.getKind() != IMessage.WARNING) throw new RuntimeException("unexpected message: " + message.toString());
			return true;
		}

		public boolean isIgnoring(Kind kind) {
			if (kind != IMessage.ERROR) return true;
			return false;
		}

		public void dontIgnore(Kind kind) {
		}
		
	}
}
