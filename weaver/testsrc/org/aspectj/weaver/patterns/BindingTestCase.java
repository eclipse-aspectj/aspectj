/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.util.*;

import org.aspectj.weaver.bcel.*;
import org.aspectj.weaver.bcel.BcelShadow;

import junit.framework.TestCase;

import org.aspectj.bridge.AbortException;
import org.aspectj.weaver.*;

/**
 * @author hugunin
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BindingTestCase extends TestCase {

	/**
	 * Constructor for ParserTestCase.
	 * @param arg0
	 */
	public BindingTestCase(String arg0) {
		super(arg0);
	}
	
	World world = new BcelWorld();
	
	
	public void testResolveBindings() {
		BindingTypePattern at = new BindingTypePattern(world.resolve("java.lang.Object"), 0);
		BindingTypePattern bt = new BindingTypePattern(world.resolve("java.lang.Object"), 1);
		
		BindingTypePattern[] all = new BindingTypePattern[] {at, bt};
		BindingTypePattern[] none = new BindingTypePattern[] {null, null};
		BindingTypePattern[] a = new BindingTypePattern[] {at, null};
		BindingTypePattern[] b = new BindingTypePattern[] {null, bt};
		
		checkBindings("this(b)",b);
		checkBindings("this(Foo)", none);
		checkBindings("this(*)", none);
		checkBindings("this(a)", a);
		
		checkBindings("args(.., a,..,b)", all);
		checkBindings("args(a,..,b, ..)", all);
		
		checkBindings("args(a,..,b)", all);
		checkBindings("args(b)", b);
		
		checkBindings("args()", none);

		checkBindings("this(a) && this(b)", all);
		
		checkBindingFailure("this(a) && this(a)", "multiple");
		//checkBindingFailure("this(a) && this(b)");

		checkBindingFailure("this(a) || this(b)", "inconsistent");
		checkBindingFailure("this(A) || this(b)", "inconsistent");
		checkBindingFailure("this(a) || this(B)", "inconsistent");
		checkBindings("this(a) || this(a)", a);
		
		checkBindings("!this(Foo)", none);
		checkBindings("!this(Foo) && this(a)", a);
		checkBindingFailure("!this(a)", "negation");
		//checkBindingFailure("this(a)");
	
	
		checkBindings("cflow(this(a))", a);
		checkBindings("cflow(this(a)) && this(b)", all);
		
		checkBindingFailure("cflow(this(a)) || this(b)", "inconsistent");
		checkBindingFailure("cflow(this(a)) && this(a)", "multiple");
		
		checkBindingFailure("!cflow(this(a))", "negation");
		
		// todo
		// this should fail since a isn't visible to if
		//checkBindingFailure("cflow(if(a != null)) && this(a)");
		//checkBinding("cflow(if(a != null) && this(a))", a);
		
	}
	

	/**
	 * Method checkBindingFailure. (assumes an env where "a" and "b" are formals).
	 * @param string
	 */
	private void checkBindingFailure(String pattern, String prefix) {
		PatternParser parser = new PatternParser(pattern);
		Pointcut p = parser.parsePointcut();
		Bindings actualBindings = new Bindings(2);
		try {
		    p.resolveBindings(makeSimpleScope(), actualBindings);
		} catch (AbortException re) {
			assertEquals(prefix, re.getIMessage().getMessage().substring(0, prefix.length()));
			//System.out.println("expected exception: " + re);
			return;
		}
		assertTrue("should have failed", false);
	}


	/**
	 * Method checkBindings.
	 * @param string
	 * @param i
	 */
	private void checkBindings(String pattern, BindingTypePattern[] expectedBindings) {
		PatternParser parser = new PatternParser(pattern);
		Pointcut p = parser.parsePointcut();
		Bindings actualBindings = new Bindings(expectedBindings.length);
		
		TestScope simpleScope = makeSimpleScope();
		p.resolveBindings(simpleScope, actualBindings);
		//System.out.println(actualBindings);
		
		new Bindings(expectedBindings).checkEquals(actualBindings, simpleScope);
	}
	
	
	public TestScope makeSimpleScope() {
		return new TestScope(new String[] {"int", "java.lang.String"}, new String[] {"a", "b"}, world);
	}
}

