/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.purejava;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class PureJavaTests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(PureJavaTests.class);
  }

  protected java.net.URL getSpecFile() {
	  return getClassResource("pureJava.xml");
//    return new File("../tests/src/org/aspectj/systemtest/pureJava/pureJava.xml");
  }


  public void test001(){
    runTest("unicodes and literals");
  }

  public void test002(){
    runTest("For Statement");
  }

  public void test003(){
    runTest("correct super call lookup for method().name()");
  }

  public void test004(){
    runTest("combined logic expression (handling coericions vs. parens)");
  }

  public void test005(){
    runTest("comment after class closes (with no new line at end)");
  }

  public void test006(){
    runTest("multi-dimensional array initializers");
  }

  public void test007(){
    runTest("probelm with the generated names of exceptions");
  }

  public void test008(){
    runTest("checks if the class field can be used on all of the primitive types");
  }

  public void test009(){
    runTest("Doesn't parse an array-returning method that throws an exception");
  }

  public void test010(){
    runTest("check that nested constructions of local classes work");
  }

  public void test011(){
    runTest("Make sure anonymous classes can have non-nullary constructors");
  }

  public void test012(){
    runTest("Full names are dropped from inner interfaces");
  }

  public void test013(){
    runTest("Making sure full names stay on static inner classes");
  }

  public void test014(){
    runTest("Not binding constructor when using more than one compilation");
  }

  public void test015(){
    runTest("return;;; is not really legal");
  }

  public void test016(){
    runTest("ajc treating Throwable as checked, issuing error if not found");
  }

  public void test017(){
    runTest("package protected classes becoming public");
  }

  public void test018(){
    runTest("IOException on windows if nul used as identifier");
  }

  public void test019(){
    runTest("NullPointerException (not compiler error) when extending non-static inner class");
  }

  public void test020(){
    runTest("compiler flags final static variable as indefinite in member assignment.");
  }

  public void test021(){
    runTest("confirm no IOException on windows if nul used as identifier");
  }

  public void test022(){
    runTest("final constructor parameter causes incorrect compiler error");
  }

  public void test023(){
    runTest("Error expected for field of type void");
  }

  public void test024(){
    runTest("Error expected for constructor in interfaces");
  }

  public void test025(){
    runTest("class name for identifier as String should provoke error");
  }

  public void test026(){
    runTest("cyclic interface inheritance not detected if no classes implement the interfaces");
  }

  public void test027(){
    runTest("cyclic class inheritance");
  }

  public void test028(){
    runTest("type and package name conflicts are resolved happily (1a)");
  }

  public void test029(){
    runTest("(fails in USEJAVAC) type and package name conflicts are resolved happily (1b)");
  }

  public void test030(){
    runTest("type and package name conflicts caught as errors (1)");
  }

  public void test031(){
    runTest("flow analysis where final variable set in another constructor");
  }

  public void test032(){
    runTest("Can construct inner classes using qualified expressions");
  }

  public void test033(){
    runTest("subclass unable to access protected static methods using type-qualified references");
  }

  public void test034(){
    runTest("Undefined inner class constructor");
  }

  public void test035(){
    runTest("classes that are package prefixes are illegal");
  }

  public void test036(){
    runTest("valid type expressions for introduced type testing");
  }

  public void test037(){
    runTest("PR591 compiler error expected when directly calling unimplemented abstract method using super");
  }

  public void test038(){
    runTest("suggested by Jacks 15.28-qualified-namestr tests");
  }

  public void test039(){
    runTest("suggested by jacks 3.7-15 all comments must be closed");
  }

  public void test040(){
    runTest("package class access not enforced outside of package");
  }

  public void test041(){
    runTest("expecting CE for ambiguous reference");
  }

  public void test042(){
    runTest("try without catch or finally");
  }

  public void test043(){
    runTest("invalid floating-point constant");
  }

  public void test044(){
    runTest("concrete aspect unable to access abstract package-private method in parent for overriding");
  }

  public void test045(){
    runTest("super reference used to disambiguate names of different but compatible types");
  }

  public void test046(){
    runTest("anonymous explicit inner constructors");
  }

  public void test047(){
    runTest("Overruning the lineStarts buffer, DO NOT EDIT THIS FILE!!!!");
  }

  public void test048(){
    runTest("no CE for unambiguous type reference");
  }

  public void test049(){
    runTest("CE for ambiguous type reference (imports)");
  }

  public void test050(){
    runTest("CE for ambiguous type reference (two type declarations)");
  }

  public void test051(){
    runTest("CE for ambiguous type reference (two inner types)");
  }

  public void test052(){
    runTest("final assignment in loop");
  }

  public void test053(){
    runTest("private super access in inners");
  }

  public void test054(){
    runTest("nested interface does not require new qualifier (8)");
  }

  public void test055(){
    runTest("nested interface does not require new qualifier (9)");
  }

  public void test056(){
    runTest("nested interface does not require new qualifier (10)");
  }

  public void test057(){
    runTest("nested interface does not require new qualifier (14)");
  }

  public void test058(){
    runTest("nested interface does not require new qualifier (15)");
  }

  public void test059(){
    runTest("nested interface does not require new qualifier (16)");
  }

  public void test060(){
    runTest("check that constructor name's match the enclosing type");
  }

  public void test061(){
    runTest("errors for not applicable or accessible methods");
  }

  public void test062(){
    runTest("import statement within class body crashes compiler");
  }

  public void test063(){
    runTest("Accessing instance fields and instance methods statically.");
  }

  public void test064(){
    runTest("Crashes when a cast is within another cast");
  }

  public void test065(){
    runTest("Crashes when a cast of the form )int) appears");
  }

  public void test066(){
    runTest("Crashes when the closing brace is reversed");
  }

  public void test067(){
    runTest("Crashes when a method name is missing in a call -- e.g. 'System.out.();'");
  }

  public void test068(){
    runTest("Crashes when a bad r-value appears.");
  }

  public void test069(){
    runTest("Two underscores as a variables causes a crash");
  }

  public void test070(){
    runTest("Crashes when assigning to a final static in an intializer and declaration");
  }

  public void test071(){
    runTest("Crashes when two dots appear instead of one");
  }

  public void test072(){
    runTest("Crashes when there're stray dots");
  }

  public void test073(){
    runTest("Stray characters cause a crash");
  }

  public void test074(){
    runTest("Colon instead of a semi-colon causes a crash");
  }

  public void test075(){
    runTest("type error in initializer caught by ajc, not javac");
  }

  public void test076(){
    runTest("Circular inheritance with classes causes a stack overflow.");
  }

  public void test077(){
    runTest("Missing ;");
  }

  public void test078(){
    runTest("cast expressions should not allow casts between ifaces and array types");
  }

  public void test079(){
    runTest("parsing errors for various bad forms of NewArrayExprs.");
  }

  public void test080(){
    runTest("good error for bad field and inner class references");
  }

  public void test081(){
    runTest("Implementing a non-interface used to crash the compiler.");
  }

  public void test082(){
    runTest("error of no return statement detected not by ajc but by javac (line 4)");
  }

  public void test083(){
    runTest("class and interface extension");
  }

  public void test084(){
    runTest("types in throws clauses");
  }

  public void test085(){
    runTest("bad switch syntax");
  }

  public void test086(){
    runTest("Referencing various things from static contexts");
  }

  public void test087(){
    runTest("Some expressions are illegal expression statements");
  }

  public void test088(){
    runTest("illegal forward reference");
  }

  public void test089(){
    runTest("protected accessibility");
  }

  public void test090(){
    runTest("parse-time illegal modifiers");
  }

  public void test091(){
    runTest("check-time illegal modifiers");
  }

  public void test092(){
    runTest("illegal synchronized stmts");
  }

  public void test093(){
    runTest("modifiers on interface members");
  }

  public void test094(){
    runTest("good errors (and not too many) for missing members");
  }

  public void test095(){
    runTest("expecting compile failures with subclass narrowing scope of superclass methods or accessing private superclass variables");
  }

  public void test096(){
    runTest("inner classes may not have static non-constant members");
  }

  public void test097(){
    runTest("flow analysis with local types");
  }

  public void test098(){
    runTest("PR584 Can construct inner classes using qualified expressions");
  }

  public void test099(){
    runTest("incrementing objects, arrays - 2");
  }

  public void test100(){
    runTest("incrementing objects, arrays CE");
  }

  public void test101(){
    runTest("incrementing objects, arrays - 3");
  }

  public void test102(){
    runTest("incrementing objects, arrays");
  }

  public void test103(){
    runTest("no circularity errors simply because of inners (1)");
  }

  public void test104(){
    runTest("no circularity errors simply because of inners (2)");
  }

  public void test105(){
    runTest("should have circular inheritance errors (1)");
  }

  public void test106(){
    runTest("should have circular inheritance errors (2)");
  }

  public void test107(){
    runTest("interface using preceding subinterface in its definition");
  }

  public void test108(){
    runTest("Parent interface using public inner interface of child in same file");
  }

  public void test109(){
    runTest("a type is not allowed to extend or implement its own innner type");
  }

  public void test110(){
    runTest("try requires block JLS 14.19");
  }

  public void test111(){
    runTest("loop expressions not declarations");
  }

  public void test112(){
    runTest("no error when public class is in file of a different name");
  }

  public void test113(){
    runTest("local variables must be final to be accessed from inner class");
  }

  public void test114(){
    runTest("final local variables may be accessed from inner class");
  }

  public void test115(){
    runTest("missing package identifier");
  }

  public void test116(){
    runTest("CE for ambiguous type reference (two files in package)");
  }

  public void test117(){
    runTest("initializer can throw so long as all constructors declare so");
  }

  public void test118(){
    runTest("interfaces may not contain initializers (bug found by jacks)");
  }

  public void test119(){
    runTest("initializers must be able to complete normally (found by jacks)");
  }

  public void test120(){
    runTest("more tests of super alone");
  }

  public void test121(){
    runTest("subclass access to enclosing super class private members");
  }

  public void test122(){
    runTest("various tests of switch bounds");
  }

  public void test123(){
    runTest("VerifyError if nested sync returning result");
  }

  public void test124(){
    runTest("assert flow");
  }

  public void test125(){
    runTest("assert flow - 2");
  }

  public void test126(){
    runTest("assert typing");
  }

  public void test127(){
    runTest("assert coverage tests [requires 1.4]");
  }

  public void test128(){
    runTest("assert coverage tests in one package [requires 1.4]");
  }

  public void test129(){
    runTest("compiling asserts in methods");
  }

  public void test130(){
    runTest("import of a class in the default package");
  }

  public void test131(){
    runTest("Referencing static interfaces with import statements");
  }

  public void test132(){
    runTest("Referencing static interfaces with import statements stars");
  }

  public void test133(){
    runTest("Referencing static interfaces with import statements stars 2");
  }

  public void test134(){
    runTest("Referencing static interfaces with import statements stars 3");
  }

  public void test135(){
    runTest("Referencing interfaces with import statements");
  }

  public void test136(){
    runTest("Referencing interfaces with import statements stars");
  }

  public void test137(){
    runTest("Referencing interfaces with import statements stars 2");
  }

  public void test138(){
    runTest("Referencing interfaces with import statements stars 3");
  }

  public void test139(){
    runTest("import any inner from interface implementor");
  }

  public void test140(){
    runTest("equals method on quoted strings");
  }

  public void test141(){
    runTest("anonymous inner class");
  }

  public void test142(){
    runTest("parsing of parenthesized 'this' (in returns)");
  }

  public void test143(){
    runTest("Strings are folded and interned correctly");
  }

  public void test144(){
    runTest("Cast binds tighter than equality tests");
  }

  public void test145(){
    runTest("Boundary base values can be parsed");
  }

  public void test146(){
    runTest("State is passed correctly across nested annonymous inners");
  }

  public void test147(){
    runTest("?: expressions should typecheck in interesting ways");
  }

  public void test148(){
    runTest("cast expressions should allow casts to/from interfaces at compile-time.");
  }

  public void test149(){
    runTest("various anonymous inner classes plus super types tests");
  }

  public void test150(){
    runTest("Various comment syntaxes should be handled.");
  }

  public void test151(){
    runTest("Abstract inner classes across package boundaries");
  }

  public void test152(){
    runTest("inner classes accessing outers and some more inner class names");
  }

  public void test153(){
    runTest("remember to cast folded values down to the proper types.");
  }

  public void test154(){
    runTest("inner classes can be built using protected constructors in super");
  }

  public void test155(){
    runTest("The current AspectJ compiler cannot parse qualified superclass constructor invocations");
  }

  public void test156(){
    runTest("More thourough test of static members using full names");
  }

  public void test157(){
    runTest("More thourough test of static members using imports");
  }

  public void test158(){
    runTest("Looking in class Java for java.lang.String WITH separate compilation");
  }

  public void test159(){
    runTest("Looking in class Java for java.lang.String WITHOUT separate compilation");
  }

  public void test160(){
    runTest("Looking in class Java for java.lang.String WITH separate compilation with packages");
  }

  public void test161(){
    runTest("Looking in class Java for java.lang.String WITHOUT separate compilation with packages");
  }

  public void test162(){
    runTest("Testing ternary operations.");
  }

  public void test163(){
    runTest("Lifting locals in switch statements.");
  }

  public void test164(){
    runTest("Getting confused when looking up method signatures");
  }

  public void test165(){
    runTest("Not recognizing the chars '\0', '\1', '\2', '\3', '\4', '\5', '\6', '\7'");
  }

  public void test166(){
    runTest("Test chars '\0', '\1', '\2', '\3', '\4', '\5', '\6', '\7' with a case statement");
  }

  public void test167(){
    runTest("Checking character values with all the unicode chars.");
  }

  public void test168(){
    runTest("Trouble finding methods with the same name and different parameter types");
  }

  public void test169(){
    runTest("Binding non-public static inner classes of interfaces in other packages");
  }

  public void test170(){
    runTest("Not recognizing the octal chars '\0', '\1', '\2', '\3', '\4', '\5', '\6', '\7'");
  }

  public void test171(){
    runTest("Members with the same name as their package cause confusion with fully-qualified names.");
  }

  public void test172(){
    runTest("Fully-qual'ed names with same start as variable names");
  }

  public void test173(){
    runTest("Fully qualifying inner classes within annonymous classes causes problems.");
  }

  public void test174(){
    runTest("Calls to methods in outer annonymous classes are being qual's incorrectly with 'this'");
  }

  public void test175(){
    runTest("Reading inner classes from source and bytecode (1) -- was failing");
  }

  public void test176(){
    runTest("Reading inner classes from source and bytecode (2)");
  }

  public void test177(){
    runTest("Reading inner classes from source and bytecode (3)");
  }

  public void test178(){
    runTest("Not lifting types correctly with bytes and shorts with ternary ops");
  }

  public void test179(){
    runTest("Not looking up methods inside of anonymous declarations correctly.");
  }

  public void test180(){
    runTest("Resolving extended classes with array parameters");
  }

  public void test181(){
    runTest("Assignments as second arguments in ternary operators.");
  }

  public void test182(){
    runTest("Conflicting inner classes with interfaces.");
  }

  public void test183(){
    runTest("confusions of casts and parens");
  }

  public void test184(){
    runTest("default constructors seen by inner classes subtyping outers");
  }

  public void test185(){
    runTest("folding fields set to anonymous instances containing self-references");
  }

  public void test186(){
    runTest("finally at the end of a method that needs to return");
  }

  public void test187(){
    runTest("overriding methods from object in interfaces and multiple-inheritance");
  }

  public void test188(){
    runTest("private fields in an outer class accessed by an inner which also extends the outer");
  }

  public void test189(){
    runTest("breaking out of a labeled block inside of an if");
  }

  public void test190(){
    runTest("abstractifying a method and getting it back through super");
  }

  public void test191(){
    runTest("Packages and static classes with the same name produce compile errors.");
  }

  public void test192(){
    runTest("Inner types must generate classfiles with only Public/Default access flags.");
  }

  public void test193(){
    runTest("Default constructors have same access as their enclosing type");
  }

  public void test194(){
    runTest("Returning primitive values matching method return type (minimal)");
  }

  public void test195(){
    runTest("Flow analysis and if(true)");
  }

  public void test196(){
    runTest("packages and generated inner types (for I.class)");
  }

  public void test197(){
    runTest("A.this exprs match by exact type matching");
  }

  public void test198(){
    runTest("Implicit this for new inner instance must be avaliable");
  }

  public void test199(){
    runTest("Inners can reference protected fields of their outer's super.");
  }

  public void test200(){
    runTest("Primitives that special case for a constant arm should work");
  }

  public void test201(){
    runTest("Parenthesized true and false don't parse");
  }

  public void test202(){
    runTest("Field sets to public fields of private fields of enclosing types");
  }

  public void test203(){
    runTest("Constant values should be stored with the correct type of their fields");
  }

  public void test204(){
    runTest("Local variables in initializers should not be treated as blank final fields");
  }

  public void test205(){
    runTest("Binops aren't allowed as update stmts in for loops");
  }

  public void test206(){
    runTest("Can't avoid doing division in case of div by zero");
  }

  public void test207(){
    runTest("Testing frames w/greater than FF locals and 7F incs (i.e., WIDE instruction)");
  }

  public void test208(){
    runTest("correct numeric literals");
  }

  public void test209(){
    runTest("invalid numeric literals");
  }

  public void test210(){
    runTest("inner types can't have the same simple name as an enclosing type");
  }

  public void test211(){
    runTest("test the unops and binops with various values");
  }

  public void test212(){
    runTest("test + and += for strings and variously typed values");
  }

  public void test213(){
    runTest("test try/catch/finally statements");
  }

  public void test214(){
    runTest("local types can be bound in the signatures of other local types");
  }

  public void test215(){
    runTest("type and package name conflicts are resolved happily (2)");
  }

  public void test216(){
    runTest("try statements work sorta like scoped items for exception flow control");
  }

  public void test217(){
    runTest("qualified this must work exactly, not based on subtypes");
  }

  public void test218(){
    runTest("nested finally blocks have interesting frame location problems");
  }

  public void test219(){
    runTest("nested synchronized blocks have interesting frame location problems");
  }

  public void test220(){
    runTest("anonymous inner classes with inner types");
  }

  public void test221(){
    runTest("qualified super call expr");
  }

  public void test222(){
    runTest("interfaces with non-explicitly static inner classes");
  }

  public void test223(){
    runTest("Operands work correctly");
  }

  public void test224(){
    runTest("simple tests of throws and for stmt typing");
  }

  public void test225(){
    runTest("test for not folding circular constants");
  }

  public void test226(){
    runTest("continue targets must be continuable");
  }

  public void test227(){
    runTest("qualified this to non-inner should be caught");
  }

  public void test228(){
    runTest("Cannot bind a name.");
  }

  public void test229(){
    runTest("interface declaration not permitted in local method scope");
  }

  public void test230(){
    runTest("Locals inside other locals, ordering of processing [eh]");
  }

  public void test231(){
    runTest("asserts");
  }

  public void test232(){
    runTest("non-constant static final fields marked as final in .class");
  }

  public void test233(){
    runTest("handle multiple nested inner classes");
  }

  public void test234(){
    runTest("advice on a static method");
  }

  public void test235(){
    runTest("inner constructor syntax causes compile error");
  }

  public void test236(){
    runTest("widening of method parameters to match javac");
  }

  public void test237(){
    runTest("parenthesized string literals matching primitive type names");
  }

  public void test238(){
    runTest("simple type coercions tests");
  }

  public void test239(){
    runTest("order of type declarations shouldn't matter");
  }

  public void test240(){
    runTest("Scanner non recognizing strictfp.");
  }

  public void test241(){
    runTest("Crashes when a lot of zeros are in front of a double variable [!!! purejava]");
  }

}

