/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc160;

import java.io.File;

import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LineNumberTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * These are tests for AspectJ1.6 - they do not require a 1.6 VM.
 */
public class Ajc160Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	/** Complex test that attempts to damage a class like a badly behaved bytecode transformer would and checks if AspectJ can cope. */
	 public void testCopingWithGarbage_pr175806_1() throws ClassNotFoundException { 
		 
		// Compile the program we are going to mess with
	    runTest("coping with bad tables");
	    
	    // Load up the class and the method 'main' we are interested in
	    JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"A");
	    Method[] meths = jc.getMethods();
	    Method oneWeWant = null;
	    for (int i = 0; i < meths.length && oneWeWant==null; i++) {
			Method method = meths[i];
			if (method.getName().equals("main")) oneWeWant = meths[i];
	    }
	    
	    /**
	     * For the main method:
		  Stack=2, Locals=3, Args_size=1
		  0:   iconst_5
		  1:   istore_1
		  2:   ldc     #18; //String 3
		  4:   astore_2
		  5:   getstatic       #24; //Field java/lang/System.out:Ljava/io/PrintStream;
		  8:   aload_2
		  9:   invokevirtual   #30; //Method java/io/PrintStream.println:(Ljava/lang/String;)V
		  12:  goto    23
		  15:  pop
		  16:  getstatic       #24; //Field java/lang/System.out:Ljava/io/PrintStream;
		  19:  iload_1
		  20:  invokevirtual   #33; //Method java/io/PrintStream.println:(I)V
		  23:  return
		 Exception table:
		  from   to  target type
		    2    15    15   Class java/lang/Exception
		
		 LineNumberTable:
		  line 4: 0
		  line 6: 2
		  line 7: 5
		  line 8: 15
		  line 9: 16
		  line 11: 23
		 LocalVariableTable:
		  Start  Length  Slot  Name   Signature
		  0      24      0    argv       [Ljava/lang/String;
		  2      22      1    i       I
		  5      10      2    s       Ljava/lang/String;
	     */
	    
	    ConstantPool cp = oneWeWant.getConstantPool();
	    ConstantPoolGen cpg = new ConstantPoolGen(cp);
	    
	    // Damage the line number table, entry 2 (Line7:5) so it points to an invalid (not on an instruction boundary) position of 6
	    oneWeWant.getLineNumberTable().getLineNumberTable()[2].setStartPC(6);

	    // Should be 'rounded down' when transforming it into a MethodGen, new position will be '5'
//	    System.out.println("BEFORE\n"+oneWeWant.getLineNumberTable().toString());
	    MethodGen toTransform = new MethodGen(oneWeWant,"A",cpg,false);
	    LineNumberTable lnt = toTransform.getMethod().getLineNumberTable();
	    assertTrue("Should have been 'rounded down' to position 5 but is "+lnt.getLineNumberTable()[2].getStartPC(), lnt.getLineNumberTable()[2].getStartPC()==5);
//	    System.out.println("AFTER\n"+lnt.toString());    
	 }
	 
	 public void testCopingWithGarbage_pr175806_2() throws ClassNotFoundException { 
	 
		// Compile the program we are going to mess with
	    runTest("coping with bad tables");
	    
	    // Load up the class and the method 'main' we are interested in
	    JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"A");
	    Method[] meths = jc.getMethods();
	    Method oneWeWant = null;
	    for (int i = 0; i < meths.length && oneWeWant==null; i++) {
			Method method = meths[i];
			if (method.getName().equals("main")) oneWeWant = meths[i];
	    }
	    // see previous test for dump of main method
	    
	    ConstantPool cp = oneWeWant.getConstantPool();
	    ConstantPoolGen cpg = new ConstantPoolGen(cp);
	    
	    // Damage the local variable table, entry 2 (" 2      22      1    i       I") so it points to an invalid start pc of 3
	    oneWeWant.getLocalVariableTable().getLocalVariable(1).setStartPC(3);

	    // Should be 'rounded down' when transforming it into a MethodGen, new position will be '2'		    
	    // This next line will go BANG with an NPE if we don't correctly round the start pc down to 2
	    MethodGen toTransform = new MethodGen(oneWeWant,"A",cpg,true);
	 }
	  

  public void testGenericAspectGenericPointcut_pr174449() { runTest("problem with generic aspect and generic pointcut");}
  public void testGenericAspectGenericPointcut_noinline_pr174449() { runTest("problem with generic aspect and generic pointcut - noinline");}
  public void testGenericMethodsAndOrdering_ok_pr171953_2() { runTest("problem with generic methods and ordering - ok");}
  public void testGenericMethodsAndOrdering_bad_pr171953_2() { runTest("problem with generic methods and ordering - bad");}
  public void testItdAndJoinpointSignatureCollection_ok_pr171953() { runTest("problem with itd and join point signature collection - ok");}
  public void testItdAndJoinpointSignatureCollection_bad_pr171953() { runTest("problem with itd and join point signature collection - bad");}
  public void testGenericMethodsAndItds_pr171952() { runTest("generic methods and ITDs");}
  //public void testUsingDecpAnnotationWithoutAspectAnnotation_pr169428() { runTest("using decp annotation without aspect annotation");}
  public void testItdsParameterizedParameters_pr170467() { runTest("itds and parameterized parameters");}
  public void testComplexGenerics_pr168044() { runTest("complex generics - 1");}
  public void testIncorrectlyMarkingFieldTransient_pr168063() { runTest("incorrectly marking field transient");}
  public void testInheritedAnnotations_pr169706() { runTest("inherited annotations");}
  public void testGenericFieldNPE_pr165885() { runTest("generic field npe");}
  public void testIncorrectOptimizationOfIstore_pr166084() { runTest("incorrect optimization of istore"); }
  public void testDualParameterizationsNotAllowed_pr165631() { runTest("dual parameterizations not allowed"); }
  	
 	public void testSuppressWarnings1_pr166238() {
		runTest("Suppress warnings1");
	}

	public void testSuppressWarnings2_pr166238() {
		runTest("Suppress warnings2");
	}
 
  public void testNullReturnedFromGetField_pr172107() { runTest("null returned from getField()"); }
	
  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc160Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc160/ajc160.xml");
  }
  
  public SyntheticRepository createRepos(File cpentry) {
	ClassPath cp = new ClassPath(cpentry+File.pathSeparator+System.getProperty("java.class.path"));
	return SyntheticRepository.getInstance(cp);
  }
  
  protected JavaClass getClassFrom(File where,String clazzname) throws ClassNotFoundException {
	SyntheticRepository repos = createRepos(where);
	return repos.loadClass(clazzname);
  }

  
}