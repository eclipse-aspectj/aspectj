/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *    Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.weaver.BcweaverTests;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.bcel.BcelWorld;

/*
 * Sample types that this program uses are:
 
import p.SimpleAnnotation;

@SimpleAnnotation(id=2)
public class AnnotatedClass {

  @SimpleAnnotation(id=3)
  public void m1() { }

  @SimpleAnnotation(id=4)
  int i;
}

 * with SimpleAnnotation defined as:

package p;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleAnnotation {
  int id();
  String fruit() default "bananas";
}

*NOTE NOTE NOTE NOTE NOTE NOTE NOTE*
If you need to rebuild the test data code, run 'ant -f build-15.xml' in the 
testdata directory.

 */
public class AnnotationPatternMatchingTestCase extends TestCase {
	
	private BcelWorld world;
	private AnnotationTypePattern fooTP,simpleAnnotationTP;
	
	private ResolvedTypeX loadType(String name) {
		if (world == null) {
		  world = new BcelWorld(BcweaverTests.TESTDATA_PATH + "/testcode.jar");
		}
		return world.resolve(name);
	}
	
	private void initAnnotationTypePatterns() {
		PatternParser p = new PatternParser("@Foo");
		fooTP = p.maybeParseAnnotationPattern();
		fooTP = fooTP.resolveBindings(makeSimpleScope(),new Bindings(3),true);
		
		p = new PatternParser("@p.SimpleAnnotation");
		simpleAnnotationTP = p.maybeParseAnnotationPattern();
		simpleAnnotationTP = simpleAnnotationTP.resolveBindings(makeSimpleScope(),new Bindings(3),true);
	}
	
	
	public void testAnnotationPatternMatchingOnTypes() {
		
		ResolvedTypeX rtx = loadType("AnnotatedClass");
        initAnnotationTypePatterns();		
		
		// One should match
		assertTrue("@Foo should not match on the AnnotatedClass",
				   fooTP.matches(rtx).alwaysFalse());
		assertTrue("@SimpleAnnotation should match on the AnnotatedClass",
				   simpleAnnotationTP.matches(rtx).alwaysTrue());

	}
	
	static class MyMessageHandler implements IMessageHandler {
		public List messages = new ArrayList();
		public boolean handleMessage(IMessage message) throws AbortException {
			messages.add(message);
			return false;
		}
		public boolean isIgnoring(Kind kind) {return false;}
	}
	
	public void testReferenceToNonAnnotationType() {
		ResolvedTypeX rtx = loadType("AnnotatedClass"); // inits the world
		PatternParser p = new PatternParser("@java.lang.String");
		
		MyMessageHandler mh = new MyMessageHandler();
		world.setMessageHandler(mh);
		AnnotationTypePattern atp = p.maybeParseAnnotationPattern();
		atp = atp.resolveBindings(makeSimpleScope(),new Bindings(3),true);
		
		assertTrue("Expected 1 error message but got "+mh.messages.size(),mh.messages.size()==1);
		
		String expected = "Type referred to is not an annotation type";
		String msg = ((IMessage)mh.messages.get(0)).toString();
		assertTrue("Expected: "+expected+" but got "+msg,msg.indexOf(expected)!=-1);
	}
	
	public void testReferenceViaFormalToNonAnnotationType() {
		ResolvedTypeX rtx = loadType("AnnotatedClass"); // inits the world
		PatternParser p = new PatternParser("a");
		
		MyMessageHandler mh = new MyMessageHandler();
		world.setMessageHandler(mh);
		AnnotationTypePattern atp = p.parseAnnotationNameOrVarTypePattern();
		atp = atp.resolveBindings(makeSimpleScope(),new Bindings(3),true);
		
		assertTrue("Expected 1 error message but got "+mh.messages.size(),mh.messages.size()==1);
		
		String expected = "Type referred to is not an annotation type";
		String msg = ((IMessage)mh.messages.get(0)).toString();
		assertTrue("Expected: "+expected+" but got "+msg,msg.indexOf(expected)!=-1);
	}
	
	public TestScope makeSimpleScope() {
		return new TestScope(new String[] {"int", "java.lang.String"}, new String[] {"a", "b"}, world);
	}

	public void testUnresolvedAnnotationTypes() {
		ResolvedTypeX rtx = loadType("AnnotatedClass");

		PatternParser p = new PatternParser("@Foo");
		AnnotationTypePattern fooTP = p.maybeParseAnnotationPattern();
		try {
			fooTP.matches(rtx);
			fail("Should have failed with illegal state exception, fooTP is not resolved");
		} catch (IllegalStateException ise) {
			// Correct!
		}
	}
	
	public void testAnnotationPatternMatchingOnMethods() {
		
		ResolvedTypeX rtx = loadType("AnnotatedClass");
		ResolvedMember aMethod = rtx.getDeclaredMethods()[1];
        
		assertTrue("Haven't got the right method, I'm looking for 'm1()': "+aMethod.getName(),
				aMethod.getName().equals("m1"));
		
		initAnnotationTypePatterns();
		
		// One should match
		assertTrue("@Foo should not match on the AnnotatedClass.m1() method",
				   fooTP.matches(aMethod).alwaysFalse());
		assertTrue("@SimpleAnnotation should match on the AnnotatedClass.m1() method",
				   simpleAnnotationTP.matches(aMethod).alwaysTrue());

	}
	
	public void testAnnotationPatternMatchingOnFields() {
		
		ResolvedTypeX rtx = loadType("AnnotatedClass");
		ResolvedMember aField = rtx.getDeclaredFields()[0];

				assertTrue("Haven't got the right field, I'm looking for 'i'"+aField.getName(),
				aField.getName().equals("i"));

		initAnnotationTypePatterns();
		
		// One should match
		assertTrue("@Foo should not match on the AnnotatedClass.i field",
				   fooTP.matches(aField).alwaysFalse());
		assertTrue("@SimpleAnnotation should match on the AnnotatedClass.i field",
				   simpleAnnotationTP.matches(aField).alwaysTrue());

	}
	
	public void testAnnotationTypeResolutionOnTypes() {
		ResolvedTypeX rtx = loadType("AnnotatedClass");
		ResolvedTypeX[] types = rtx.getAnnotationTypes();
		assertTrue("Did not expect null",types!=null);
		assertTrue("Expected 1 entry but got "+types.length,types.length==1);
		assertTrue("Should be 'p.SimpleAnnotation' but is "+types[0],
				types[0].equals(world.resolve("p.SimpleAnnotation")));
	}
	
	public void testAnnotationTypeResolutionOnMethods() {
		ResolvedTypeX rtx = loadType("AnnotatedClass");

		ResolvedMember aMethod = rtx.getDeclaredMethods()[1];
		assertTrue("Haven't got the right method, I'm looking for 'm1()': "+aMethod.getName(),
				aMethod.getName().equals("m1"));
		
		ResolvedTypeX[] types = aMethod.getAnnotationTypes();
		assertTrue("Did not expect null",types!=null);
		assertTrue("Expected 1 entry but got "+types.length,types.length==1);
		assertTrue("Should be 'p.SimpleAnnotation' but is "+types[0],
				types[0].equals(world.resolve("p.SimpleAnnotation")));
	}
	
	public void testAnnotationTypeResolutionOnFields() {
		ResolvedTypeX rtx = loadType("AnnotatedClass");

		ResolvedMember aField = rtx.getDeclaredFields()[0];

		assertTrue("Haven't got the right field, I'm looking for 'i'"+aField.getName(),
				   aField.getName().equals("i"));
				
		ResolvedTypeX[] types = aField.getAnnotationTypes();
		assertTrue("Did not expect null",types!=null);
		assertTrue("Expected 1 entry but got "+types.length,types.length==1);
		assertTrue("Should be 'p.SimpleAnnotation' but is "+types[0],
				types[0].equals(world.resolve("p.SimpleAnnotation")));
	}
	
	public void testWildPatternMatchingOnTypes() {
		
		ResolvedTypeX rtx = loadType("AnnotatedClass");
        initAnnotationTypePatterns();		
        
        // Let's create something wild
		PatternParser p = new PatternParser("@Foo || @Boo");
		AnnotationTypePattern ap = p.maybeParseAnnotationPattern();
		ap = ap.resolveBindings(makeSimpleScope(),new Bindings(3),true);
		assertTrue("shouldnt match the type AnnotatedClass",ap.matches(rtx).alwaysFalse());
		
		
		p = new PatternParser("@p.SimpleAnnotation || @Boo");
		ap = p.maybeParseAnnotationPattern();
		ap = ap.resolveBindings(makeSimpleScope(),new Bindings(3),true);
		assertTrue("should match the type AnnotatedClass",ap.matches(rtx).alwaysTrue());
	}

}
