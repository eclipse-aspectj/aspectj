/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * ******************************************************************/
package org.aspectj.systemtest.pre10x;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class AjcPre10xTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(AjcPre10xTests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("pre10x.xml");
	}


	public void test001(){
		runTest("Using 'aspect' as identifier is legal TODO");
	}

	public void test002(){
		runTest("Using 'pointcut' as identifier is legal TODO");
	}

	public void test003(){
		runTest("CF expected when enclosing class superclass used as this qualifier in inner class");
	}

	public void test004(){
		runTest("enclosing class may be used as this qualifier in inner class");
	}

	public void test005(){
		runTest("reasonable error for crosscut reference with no formals specified");
	}

	public void test006(){
		runTest("reasonable error for introduction on type whose source isn't found");
	}

	public void test007(){
		runTest("handle errors in crosscut designators, insist that they end with a semicolon");
	}

	public void test008(){
		runTest("try to return from a before, after, after throwing and after returning");
	}

	public void test009(){
		runTest("the designator has a wildcard for method name but no return type specified");
	}

	public void test010(){
		runTest("the designator for the introduction has no type after the | charcter");
	}

	public void test011(){
		runTest("crosscut signature does not match");
	}

	public void test012(){
		runTest("proper exit conditions when errors fall through to javac");
	}

	public void test013(){
		runTest("mismatched parens on advice (wasn't binding Tester)");
	}

	public void test014(){
		runTest("Non-static advice silently ignored");
	}

	public void test015(){
		runTest("extra closing brace");
	}

	public void test016(){
		runTest("decent errors for around return type not matching target point");
	}

	public void test017(){
		runTest("eachobject: can't call new on an aspect of");
	}

	public void test018(){
		runTest("eachobject: only zero-argument constructors allowed in an aspect");
	}

	public void test019(){
		runTest("eachobject: can't extend a concrete aspect");
	}

	public void test020(){
		runTest("instanceof used without a class");
	}

	public void test021(){
		runTest("wildcard used for returns clause");
	}

	public void test022(){
		runTest("no return statement in around advice");
	}

	public void test023(){
		runTest("inner aspects must be static (no longer matches PR#286)");
	}

	public void test024(){
		runTest("Casting class declarations as interfaces");
	}

	public void test025(){
		runTest("omits a variable name and crashes with a null pointer");
	}

	public void test026(){
		runTest("Not generating an error for using new as a method name");
	}

	public void test027(){
		runTest("ClassCastException on the int literal");
	}

	public void test028(){
		runTest("Wrong strictfp keyword usage in interface function prototype [TODO: move to errors]");
	}

	public void test029(){
		runTest("Wrong strictfp keyword usage in field declaration [TODO: move to errors]");
	}

	public void test030(){
		runTest("Wrong strictfp keyword usage in constructor declaration [TODO: move to errors]");
	}

	public void test031(){
		runTest("Incorrect static casts to primitively foldable arguments should not crash the compiler.");
	}

	public void test032(){
		runTest("Dominates with commas should signal an error.");
	}

	public void test033(){
		runTest("stack overflow with recursive crosscut specifier");
	}

	public void test034(){
		runTest("Throwing a NullPointerException when formals can't be bound in named pointcut");
	}

	public void test035(){
		runTest("disallow defining more than one pointcut with the same name");
	}

	// With intro of record in Java14 the error messages here change
	//  public void test036(){
	//    runTest("pre 0.7 introduction form outside aspect body causes an EmptyStackException");
	//  }
	//
	//  public void test037(){
	//    runTest("a class can't extend an aspect");
	//  }
	//
	//  public void test038(){
	//    runTest("a before() clause at the class-level causes an EmptyStackException");
	//  }
	//
	//  public void test039(){
	//    runTest("an after() clause at the class-level causes an EmptyStackException");
	//  }
	//
	// public void test040(){
	//	runTest("an around() clause at the class-level causes an EmptyStackException");
	// }

	public void test041(){
		runTest("Doesn't detect cyclic inheritance of aspects.");
	}

	public void test042(){
		runTest("Binds the pointcut formals to member variables instead of pointcut formals.");
	}

	public void test043(){
		runTest("ambiguous formal in formals pattern");
	}

	public void test044(){
		runTest("good error for field name instead of type name");
	}

	public void test045(){
		runTest("errors in aspect inheritance - 1");
	}

	public void test046(){
		runTest("errors in aspect inheritance - 2");
	}

	public void test047(){
		runTest("errors in aspect inheritance - 3");
	}

	public void test048(){
		runTest("errors in aspect inheritance - 4");
	}

	public void test049(){
		runTest("circular dominates leading to irresolvable advice precedence");
	}

	public void test050(){
		runTest("Should issue an error for using 'class' instead of 'aspect'");
	}

	public void test051(){
		runTest("Should signal an error when we need an exposed value but don't provide it");
	}

	public void test052(){
		runTest("StackOverFlowException with circular +implements's.");
	}

	public void test053(){
		runTest("Introducing protected methods is causing a crash");
	}

	public void test054(){
		runTest("Introducing protected fields is causing a crash");
	}

	public void test055(){
		runTest("two classes with the same fully-qualified names [eachjvm]");
	}

	public void test056(){
		runTest("Undefined pointcuts were throwing exceptions in 07b11 [callsto]");
	}

	public void test057(){
		runTest("advice on abstract pointcuts");
	}

	public void test058(){
		runTest("Whoops, I forgot to put a class in the field access PCD.");
	}

	public void test059(){
		runTest("the arounds return something but there is no returns statement");
	}

	public void test060(){
		runTest("multiple conflicting introductions");
	}

	public void test061(){
		runTest("referencing non-static pointcuts in outer aspects");
	}

	public void test062(){
		runTest("javac correct compiler error if there is no return in around returning result");
	}

	public void test063(){
		runTest("should give an error for introducing two members with the same name");
	}

	public void test064(){
		runTest("wimpy test for undeclared and uncaught exceptions");
	}

	public void test065(){
		runTest("Given non-matching TypePattern, CE flags use of non-introduced method rather than failure to introduce");
	}

	public void test066(){
		runTest("Compiler should suggest using aspect when advice, pointcuts, or introduction is in a class");
	}

	public void test067(){
		runTest("declare error working with pointcut and-not (amp,amp,bang)");
	}

	public void test068(){
		runTest("aspect as member of interface (private and protected)");
	}

	public void test069(){
		runTest("introduced inner interfaces - compile should fail to bind interface name outside of Aspect or if implementing method is not public");
	}

	public void test070(){
		runTest("aspects may not implement Serializable or Cloneable");
	}

	public void test071(){
		runTest("explicit constructor calls can throw exceptions");
	}

	public void test072(){
		runTest("bad proceed args good error messages");
	}

}

