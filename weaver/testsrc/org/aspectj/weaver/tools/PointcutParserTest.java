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

import junit.framework.TestCase;

/**
 * Test cases for the PointcutParser class
 */
public class PointcutParserTest extends TestCase {

	public void testGetAllSupportedPointcutPrimitives() {
		Set s = PointcutParser.getAllSupportedPointcutPrimitives();
		assertEquals("Should be 14 elements in the set",14,s.size());
		assertFalse("Should not contain if pcd",s.contains(PointcutPrimitives.IF));
		assertFalse("Should not contain cflow pcd",s.contains(PointcutPrimitives.CFLOW));
		assertFalse("Should not contain cflowbelow pcd",s.contains(PointcutPrimitives.CFLOW_BELOW));
	}
	
	public void testEmptyConstructor() {
		PointcutParser parser = new PointcutParser();
		Set s = parser.getSupportedPrimitives();
		assertEquals("Should be 14 elements in the set",14,s.size());
		assertFalse("Should not contain if pcd",s.contains(PointcutPrimitives.IF));
		assertFalse("Should not contain cflow pcd",s.contains(PointcutPrimitives.CFLOW));
		assertFalse("Should not contain cflowbelow pcd",s.contains(PointcutPrimitives.CFLOW_BELOW));
	}
	
	public void testSetConstructor() {
		Set p = PointcutParser.getAllSupportedPointcutPrimitives();
		PointcutParser parser = new PointcutParser(p);
		assertEquals("Should use the set we pass in",p,parser.getSupportedPrimitives());
		Set q = new HashSet();
		q.add(PointcutPrimitives.ARGS);
		parser = new PointcutParser(q);
		assertEquals("Should have only one element in set",1,parser.getSupportedPrimitives().size());
		assertEquals("Should only have ARGS pcd",PointcutPrimitives.ARGS,
				parser.getSupportedPrimitives().iterator().next());
	}
	
	public void testParsePointcutExpression() {
		PointcutParser p = new PointcutParser();
		PointcutExpression pEx = p.parsePointcutExpression(
				"(adviceexecution() || execution(* *.*(..)) || handler(Exception) || " +
				"call(Foo Bar+.*(Goo)) || get(* foo) || set(Foo+ (Goo||Moo).s*) || " +
				"initialization(Foo.new(..)) || preinitialization(*.new(Foo,..)) || " +
				"staticinitialization(org.xzy.abc..*)) && (this(Foo) || target(Boo) ||" +
				"args(A,B,C)) && !handler(X)");
		try {
			pEx = p.parsePointcutExpression("gobble-de-gook()");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException ex) {}
	}
	
	public void testParseIfPCD() {
		PointcutParser p = new PointcutParser();
		try {
			p.parsePointcutExpression("if(true)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("if pointcuts and reference pointcuts are not supported"));
		}
	}
	
	public void testParseCflowPCDs() {
		PointcutParser p = new PointcutParser();
		try {
			p.parsePointcutExpression("cflow(this(t))");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("cflow and cflowbelow are not supported"));
		}
		try {
			p.parsePointcutExpression("cflowbelow(this(t))");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("cflow and cflowbelow are not supported"));
		}	
	}
	
	public void testParseReferencePCDs() {
		PointcutParser p = new PointcutParser();
		try {
			p.parsePointcutExpression("bananas(x)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("if pointcuts and reference pointcuts are not supported"));
		}	
	}

	public void testParseUnsupportedPCDs() {
		Set s = new HashSet();
		PointcutParser p = new PointcutParser(s);
		try {
			p.parsePointcutExpression("args(x)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("args is not supported"));
		}	
		try {
			p.parsePointcutExpression("within(x)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("within is not supported"));
		}	
		try {
			p.parsePointcutExpression("withincode(new(..))");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("withincode is not supported"));
		}	
		try {
			p.parsePointcutExpression("handler(Exception)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("handler is not supported"));
		}	
		try {
			p.parsePointcutExpression("this(X)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("this is not supported"));
		}	
		try {
			p.parsePointcutExpression("target(X)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("target is not supported"));
		}	
		try {
			p.parsePointcutExpression("this(X) && target(Y)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("this is not supported"));
		}	
		try {
			p.parsePointcutExpression("this(X) || target(Y)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("this is not supported"));
		}	
		try {
			p.parsePointcutExpression("!this(X)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("this is not supported"));
		}	
		try {
			p.parsePointcutExpression("call(* *.*(..))");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("call is not supported"));
		}	
		try {
			p.parsePointcutExpression("execution(* *.*(..))");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("execution is not supported"));
		}	
		try {
			p.parsePointcutExpression("get(* *)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("get is not supported"));
		}	
		try {
			p.parsePointcutExpression("set(* *)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("set is not supported"));
		}	
		try {
			p.parsePointcutExpression("initialization(new(..))");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("initialization is not supported"));
		}	
		try {
			p.parsePointcutExpression("preinitialization(new(..))");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("preinitialization is not supported"));
		}	
		try {
			p.parsePointcutExpression("staticinitialization(T)");
			fail("Expected UnsupportedOperationException");
		} catch(UnsupportedOperationException ex) {
			assertTrue(ex.getMessage().startsWith("staticinitialization is not supported"));
		}	
	}	
}
