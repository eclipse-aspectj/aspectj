/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc151;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.systemtest.ajc150.GenericsTests;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc151Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

  // Some @DeclareParents testing
  public void testAtDecp_1() { runTest("atDecp - simple");}
  public void testAtDecp_2() { runTest("atDecp - annotation");}
  public void testAtDecp_3() { runTest("atDecp - binary interface");}
  public void testAtDecp_4() { runTest("atDecp - binary interface - 2");}

  public void testAnnotationsAndItds_pr98901() { runTest("annotations and itds");}
  public void testAnnotationsAndItds_pr98901_2() { runTest("annotations and itds - 2");}
  public void testCircularGenerics_pr133307() { runTest("circular generics");}
  public void testDeca() { runTest("doubly annotating a method with declare");}	
  public void testDeca2() { runTest("doubly annotating a method with declare - 2");}	
  public void testCrashingWithASM_pr132926_1() { runTest("crashing on annotation type resolving with asm - 1");}
  public void testCrashingWithASM_pr132926_2() { runTest("crashing on annotation type resolving with asm - 2");}
  public void testCrashingWithASM_pr132926_3() { runTest("crashing on annotation type resolving with asm - 3");}
  public void testGenericAdviceParameters_pr123553()    { runTest("generic advice parameters");}
  public void testMemberTypesInGenericTypes_pr122458()    { runTest("member types in generic types");}
  public void testMemberTypesInGenericTypes_pr122458_2()  { runTest("member types in generic types - 2");}
  public void testNPEOnDeclareAnnotation_pr123695() { runTest("Internal nullptr exception with complex declare annotation");}
  public void testHasMemberPackageProblem_pr124105() { runTest("hasMember problems with packages");}
  public void testDifferentNumbersofTVars_pr124803() { runTest("generics and different numbers of type variables");}
  public void testDifferentNumbersofTVars_pr124803_2() { runTest("generics and different numbers of type variables - classes");}
  public void testParameterizedCollectionFieldMatching_pr124808() { runTest("parameterized collection fields matched via pointcut");}
  public void testGenericAspectsAndAnnotations_pr124654() { runTest("generic aspects and annotations");}
  public void testCallInheritedGenericMethod_pr124999() { runTest("calling inherited generic method from around advice");}
  public void testIncorrectlyReferencingPointcuts_pr122452()    { runTest("incorrectly referencing pointcuts");}
  public void testIncorrectlyReferencingPointcuts_pr122452_2()    { runTest("incorrectly referencing pointcuts - 2");}
  public void testInlinevisitorNPE_pr123901() { runTest("inlinevisitor NPE");}
  //public void testExposingWithintype_enh123423() { runTest("exposing withintype");}
  //public void testMissingImport_pr127299() { runTest("missing import gives funny message");}
  public void testUnusedInterfaceMessage_pr120527() { runTest("incorrect unused interface message");}
  public void testAtAspectInheritsAdviceWithTJPAndThis_pr125699 () { runTest("inherit advice with this() and thisJoinPoint");  }
  public void testAtAspectInheritsAdviceWithTJPAndThis_pr125699_2 () {runTest("inherit advice with this() and thisJoinPoint - 2");  }
  public void testBrokenLTW_pr128744() { runTest("broken ltw"); }
  
  public void testAtAspectNoInvalidAbsoluteTypeName_pr126560() {
	  runTest("@AJ deow doesn't throw an invalidAbsoluteTypeName when specify type in the same package");
  }
  
  public void testAtAspectNoInvalidAbsoluteTypeName_pr126560_2() {
	  runTest("@AJ deow doesn't throw an invalidAbsoluteTypeName when specify type in the same file");
  }
  
  public void testArrayindexoutofbounds_pr129566() { 
	  runTest("arrayindexoutofbounds");
	  // public class SkipList<T extends Comparable> extends Object implements Set<T>, Iterable<T>
	  GenericsTests.verifyClassSignature(ajc,"common.SkipList","<T::Ljava/lang/Comparable;>Ljava/lang/Object;Ljava/util/Set<TT;>;Ljava/lang/Iterable<TT;>;");
	  // protected class SkipListElement<E> extends Object
	  GenericsTests.verifyClassSignature(ajc,"common.SkipList$SkipListElement","<E:Ljava/lang/Object;>Ljava/lang/Object;");
	  // protected class SkipListIterator<E> implements Iterator<T>
	  GenericsTests.verifyClassSignature(ajc,"common.SkipList$SkipListIterator","<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Iterator<TT;>;");
  }
  
  public void testMixingNumbersOfTypeParameters_pr125080()   { 
	  runTest("mixing numbers of type parameters");
	  GenericsTests.verifyClassSignature(ajc,"AspectInterface","<T:Ljava/lang/Object;S:Ljava/lang/Object;>Ljava/lang/Object;");
	  GenericsTests.verifyClassSignature(ajc,"AbstractAspect","<T:Ljava/lang/Object;>Ljava/lang/Object;LAspectInterface<TT;Ljava/lang/Integer;>;");
	  GenericsTests.verifyClassSignature(ajc,"ConcreteAspect","LAbstractAspect<Ljava/lang/String;>;");
  }
  
  public void testMixingNumbersOfTypeParameters_pr125080_2() { 
	  runTest("mixing numbers of type parameters - 2"); 
	  GenericsTests.verifyClassSignature(ajc,"AspectInterface","<T:Ljava/lang/Object;S:Ljava/lang/Number;>Ljava/lang/Object;");
	  GenericsTests.verifyClassSignature(ajc,"AbstractAspect","<T:Ljava/lang/Object;>Ljava/lang/Object;LAspectInterface<TT;Ljava/lang/Integer;>;");
	  GenericsTests.verifyClassSignature(ajc,"ConcreteAspect","LAbstractAspect<LStudent;>;");
  }
  
  public void testIProgramElementMethods_pr125295() {
	  runTest("new IProgramElement methods");  
  	  IHierarchy top = AsmManager.getDefault().getHierarchy();

  	  IProgramElement pe = top.findElementForType("pkg","foo");
  	  assertNotNull("Couldn't find 'foo' element in the tree",pe);
  	  // check that the defaults return the fully qualified arg
  	  assertEquals("foo(int,java.lang.Object)",pe.toLabelString());
  	  assertEquals("C.foo(int,java.lang.Object)",pe.toLinkLabelString());
  	  assertEquals("foo(int,java.lang.Object)",pe.toSignatureString());
  	  // check that can get hold of the non qualified args
  	  assertEquals("foo(int,Object)",pe.toLabelString(false));
  	  assertEquals("C.foo(int,Object)",pe.toLinkLabelString(false));
  	  assertEquals("foo(int,Object)",pe.toSignatureString(false));

  	  IProgramElement pe2 = top.findElementForType("pkg","printParameters");
  	  assertNotNull("Couldn't find 'printParameters' element in the tree",pe2);
  	  // the argument is org.aspectj.lang.JoinPoint, check that this is added
  	  assertFalse("printParameters method should have arguments",pe2.getParameterTypes().isEmpty());	  
  }

  public void testParameterizedEnum_pr126316() {
	  runTest("E extends Enum(E) again");
  }
  
  public void testSwallowedException() {
	  runTest("swallowed exceptions");
  }
  
  public void testAtAspectVerifyErrorWithAfterThrowingAndthisJoinPoint_pr122742() {
	  runTest("@AJ VerifyError with @AfterThrowing and thisJoinPoint argument");
  }
  
  public void testAtAspectVerifyErrorWithAfterReturningAndthisJoinPoint_pr122742() {
	  runTest("@AJ VerifyError with @AfterReturning and thisJoinPoint argument");
  }
  
  public void testSwallowedExceptionIgnored() {
	  runTest("swallowed exceptions with xlint");
  }
  
  public void testGenericAspectWithUnknownType_pr131933() {
	  runTest("no ClassCastException with generic aspect and unknown type");
  }
  
  public void testStructureModelForGenericITD_pr131932() {
 	  //AsmManager.setReporting("c:/debug.txt",true,true,true,true);
	  runTest("structure model for generic itd");
	  IHierarchy top = AsmManager.getDefault().getHierarchy();
 	   
  	  // get the IProgramElements corresponding to the ITDs and classes
  	  IProgramElement foo = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.CLASS,"Foo");
  	  assertNotNull("Couldn't find Foo element in the tree",foo);
  	  IProgramElement bar = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.CLASS,"Bar");
  	  assertNotNull("Couldn't find Bar element in the tree",bar);

  	  IProgramElement method = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.INTER_TYPE_METHOD,"Bar.getFirst()");  	   	 
  	  assertNotNull("Couldn't find 'Bar.getFirst()' element in the tree",method);
  	  IProgramElement field = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.INTER_TYPE_FIELD,"Bar.children");  	   	 
  	  assertNotNull("Couldn't find 'Bar.children' element in the tree",field);
  	  IProgramElement constructor = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR,"Foo.Foo(List<T>)");  	   	 
  	  assertNotNull("Couldn't find 'Foo.Foo(List<T>)' element in the tree",constructor);
  	  
  	  // check that the relationship map has 'itd method declared on bar'
  	  List matches = AsmManager.getDefault().getRelationshipMap().get(method);
  	  assertNotNull("itd Bar.getFirst() should have some relationships but does not",matches);
  	  assertTrue("method itd should have one relationship but has " + matches.size(), matches.size() == 1);
  	  List matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("itd Bar.getFirst() should have one target but has " + matchesTargets.size(),matchesTargets.size() == 1);
  	  IProgramElement target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the Bar class but is IPE with label "
  			  + target.toLabelString(),bar,target);

  	  // check that the relationship map has 'itd field declared on bar'
  	  matches = AsmManager.getDefault().getRelationshipMap().get(field);
  	  assertNotNull("itd Bar.children should have some relationships but does not",matches);
  	  assertTrue("field itd should have one relationship but has " + matches.size(), matches.size() == 1);
  	  matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("itd Bar.children should have one target but has " + matchesTargets.size(),matchesTargets.size() == 1);
  	  target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the Bar class but is IPE with label "
  			  + target.toLabelString(),bar,target);

  	  // check that the relationship map has 'itd constructor declared on foo'
  	  matches = AsmManager.getDefault().getRelationshipMap().get(constructor);
  	  assertNotNull("itd Foo.Foo(List<T>) should have some relationships but does not",matches);
  	  assertTrue("constructor itd should have one relationship but has " + matches.size(), matches.size() == 1);
  	  matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("itd Foo.Foo(List<T>) should have one target but has " + matchesTargets.size(),matchesTargets.size() == 1);
  	  target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the Foo class but is IPE with label "
  			  + target.toLabelString(),foo,target);
  	  
  	  // check that the relationship map has 'bar aspect declarations method and field itd'
  	  matches = AsmManager.getDefault().getRelationshipMap().get(bar);
  	  assertNotNull("Bar should have some relationships but does not",matches);
  	  assertTrue("Bar should have one relationship but has " + matches.size(), matches.size() == 1);
  	  matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("Bar should have two targets but has " + matchesTargets.size(),matchesTargets.size() == 2);
  	  for (Iterator iter = matchesTargets.iterator(); iter.hasNext();) {
		  String element = (String) iter.next();
		  target = AsmManager.getDefault().getHierarchy().findElementForHandle(element);
		  if (!target.equals(method) && !target.equals(field)) {
			  fail("Expected rel target to be " + method.toLabelString() + " or " + field.toLabelString() 
					+ ", found " + target.toLabelString());
		  }
	  }

  	  // check that the relationship map has 'foo aspect declarations constructor itd'
 	  matches = AsmManager.getDefault().getRelationshipMap().get(foo);
  	  assertNotNull("Foo should have some relationships but does not",matches);
  	  assertTrue("Foo should have one relationship but has " + matches.size(), matches.size() == 1);
  	  matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("Foo should have one target but has " + matchesTargets.size(),matchesTargets.size() == 1);
 	  target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the Foo.Foo(List<T>) itd but is IPE with label "
  			  + target.toLabelString(),constructor,target);
  }
  
  
  public void testDeclareAnnotationAppearsInStructureModel_pr132130() {
	  //AsmManager.setReporting("c:/debug.txt",true,true,true,true);
	  runTest("declare annotation appears in structure model when in same file");
	  IHierarchy top = AsmManager.getDefault().getHierarchy();
	  
  	  // get the IProgramElements corresponding to the different code entries
  	  IProgramElement decam = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.DECLARE_ANNOTATION_AT_METHOD,"declare @method: * debit(..) : @Secured(role = \"supervisor\")");  	   	 
  	  assertNotNull("Couldn't find 'declare @method' element in the tree",decam);
  	  IProgramElement method = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.METHOD,"debit(long,long)");
  	  assertNotNull("Couldn't find the 'debit(long,long)' method element in the tree",method);
  	  IProgramElement decac = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.DECLARE_ANNOTATION_AT_CONSTRUCTOR,"declare @constructor: BankAccount+.new(..) : @Secured(role = \"supervisor\")");  	   	 
  	  assertNotNull("Couldn't find 'declare @constructor' element in the tree",decac);
  	  IProgramElement ctr = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.CONSTRUCTOR,"BankAccount(String,int)");
  	  assertNotNull("Couldn't find the 'BankAccount(String,int)' constructor element in the tree",ctr);

  	  
  	  // check that decam has a annotates relationship with the debit method
  	  List matches = AsmManager.getDefault().getRelationshipMap().get(decam);	
  	  assertNotNull("'declare @method' should have some relationships but does not",matches);
  	  assertTrue("'declare @method' should have one relationships but has " + matches.size(),matches.size()==1);
  	  List matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("'declare @method' should have one targets but has" + matchesTargets.size(),matchesTargets.size()==1);
  	  IProgramElement target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the 'debit(long,long)' method but is IPE with label "
  			  + target.toLabelString(),method,target);
  	  
  	  // check that the debit method has an annotated by relationship with the declare @method
  	  matches = AsmManager.getDefault().getRelationshipMap().get(method);	
  	  assertNotNull("'debit(long,long)' should have some relationships but does not",matches);
  	  assertTrue("'debit(long,long)' should have one relationships but has " + matches.size(),matches.size()==1);
  	  matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("'debit(long,long)' should have one targets but has" + matchesTargets.size(),matchesTargets.size()==1);
  	  target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the 'declare @method' ipe but is IPE with label "
  			  + target.toLabelString(),decam,target);

  	  // check that decac has a annotates relationship with the constructor
  	  matches = AsmManager.getDefault().getRelationshipMap().get(decac);	
  	  assertNotNull("'declare @method' should have some relationships but does not",matches);
  	  assertTrue("'declare @method' should have one relationships but has " + matches.size(),matches.size()==1);
  	  matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("'declare @method' should have one targets but has" + matchesTargets.size(),matchesTargets.size()==1);
  	  target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the 'debit(long, long)' method but is IPE with label "
  			  + target.toLabelString(),ctr,target);
  	  
  	  // check that the constructor has an annotated by relationship with the declare @constructor
  	  matches = AsmManager.getDefault().getRelationshipMap().get(ctr);	
  	  assertNotNull("'debit(long, long)' should have some relationships but does not",matches);
  	  assertTrue("'debit(long, long)' should have one relationships but has " + matches.size(),matches.size()==1);
  	  matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("'debit(long, long)' should have one targets but has" + matchesTargets.size(),matchesTargets.size()==1);
  	  target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the 'declare @method' ipe but is IPE with label "
  			  + target.toLabelString(),decac,target);


  }
  
  /*
   * @AspectJ bugs and enhancements
   */
//  public void testAtAspectInheritsAdviceWithTJPAndThis_pr125699 () {
//	  runTest("inherit adivce with this() and thisJoinPoint"); 
//  }
  
  public void testAtAspectInheritsAbstractPointcut_pr125810 () {
	  runTest("warning when inherited pointcut not made concrete"); 
  }
  
  public void testAtAspectDEOWInStructureModel_pr120356() {
	  //AsmManager.setReporting("c:/debug.txt",true,true,true,true);
	  runTest("@AJ deow appear correctly when structure model is generated");  
  	  IHierarchy top = AsmManager.getDefault().getHierarchy();
  	   
  	  // get the IProgramElements corresponding to the @DeclareWarning statement
  	  // and the method it matches.
  	  IProgramElement warningMethodIPE = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.METHOD,"warningMethod()");  	   	 
  	  assertNotNull("Couldn't find 'warningMethod()' element in the tree",warningMethodIPE);
  	  IProgramElement atDeclareWarningIPE = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.FIELD,"warning");
  	  assertNotNull("Couldn't find @DeclareWarning element in the tree",atDeclareWarningIPE);

  	  // check that the method has a matches declare relationship with @DeclareWarning
  	  List matches = AsmManager.getDefault().getRelationshipMap().get(warningMethodIPE);	
  	  assertNotNull("warningMethod should have some relationships but does not",matches);
  	  assertTrue("warningMethod should have one relationships but has " + matches.size(),matches.size()==1);
  	  List matchesTargets = ((Relationship)matches.get(0)).getTargets();
  	  assertTrue("warningMethod should have one targets but has" + matchesTargets.size(),matchesTargets.size()==1);
  	  IProgramElement target = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchesTargets.get(0));
  	  assertEquals("target of relationship should be the @DeclareWarning 'warning' but is IPE with label "
  			  + target.toLabelString(),atDeclareWarningIPE,target);
  	  
  	  // check that the @DeclareWarning has a matches relationship with the warningMethod
  	  List matchedBy = AsmManager.getDefault().getRelationshipMap().get(atDeclareWarningIPE);
  	  assertNotNull("@DeclareWarning should have some relationships but does not",matchedBy);
  	  assertTrue("@DeclareWarning should have one relationship but has " + matchedBy.size(), matchedBy.size() == 1);
  	  List matchedByTargets = ((Relationship)matchedBy.get(0)).getTargets();
  	  assertTrue("@DeclareWarning 'matched by' relationship should have one target " +
  	  		"but has " + matchedByTargets.size(), matchedByTargets.size() == 1);
  	  IProgramElement matchedByTarget = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchedByTargets.get(0));
  	  assertEquals("target of relationship should be the warningMethod but is IPE with label "
  			  + matchedByTarget.toLabelString(),warningMethodIPE,matchedByTarget);
  	  
  	  // get the IProgramElements corresponding to the @DeclareError statement
  	  // and the method it matches.
  	  IProgramElement errorMethodIPE = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.METHOD,"badMethod()");  	   	 
  	  assertNotNull("Couldn't find 'badMethod()' element in the tree",errorMethodIPE);
  	  IProgramElement atDeclarErrorIPE = top.findElementForLabel(top.getRoot(),
  			  IProgramElement.Kind.FIELD,"error");
  	  assertNotNull("Couldn't find @DeclareError element in the tree",atDeclarErrorIPE);

  	  // check that the @DeclareError has a matches relationship with the badMethod
  	  List matchedByE = AsmManager.getDefault().getRelationshipMap().get(atDeclarErrorIPE);
  	  assertNotNull("@DeclareError should have some relationships but does not",matchedByE);
  	  assertTrue("@DeclareError should have one relationship but has " + matchedByE.size(), matchedByE.size() == 1);
  	  List matchedByTargetsE = ((Relationship)matchedByE.get(0)).getTargets();
  	  assertTrue("@DeclareError 'matched by' relationship should have one target " +
  	  		"but has " + matchedByTargetsE.size(), matchedByTargetsE.size() == 1);
  	  IProgramElement matchedByTargetE = AsmManager.getDefault().getHierarchy().findElementForHandle((String)matchedByTargetsE.get(0));
  	  assertEquals("target of relationship should be the badMethod but is IPE with label "
  			  + matchedByTargetE.toLabelString(),errorMethodIPE,matchedByTargetE);

  }
  
  public void testAtAspectNoNPEWithDEOWWithoutStructureModel_pr120356() {
	  runTest("@AJ no NPE with deow when structure model isn't generated"); 
  }
  
  public void testAtAspectWithoutJoinPointImport_pr121616() {
	  runTest("@AJ without JoinPoint import");
  }
  
  public void testAtAspectDeclareParentsRetainsFieldState_pr122370() {
	  runTest("@AJ declare parents retains field state");
  }
  
  public void testAtAspectNoNPEWithPcdContainingOrThisAndWildcard_pr128237() {
	  runTest("@AJ no npe with pointcut containing or, this and a wildcard");
  }
  
  /*
   * Load-time weaving bugs and enhancements
   */
  public void testEmptyPointcutInAtAspectJ_pr125475 () {
	  runTest("define empty pointcut using an annotation"); 
  }

  public void testEmptyPointcutInAtAspectJ_pr125475_2() {
	  runTest("define empty pointcut using an annotation - 2"); 
  }
  
  public void testEmptyPointcutInAtAspectJWithLTW_pr125475 () {
	  runTest("define empty pointcut using aop.xml"); 
  }

  public void testGenericAspectsWithAnnotationTypeParameters() {
	  runTest("Generic aspects with annotation type parameters");
  }
  
  public void testPointcutInterfaces_pr130869() {
	  runTest("Pointcut interfaces");
  }
  
  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc151Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc151/ajc151.xml");
  }

  
}