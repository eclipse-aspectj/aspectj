/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.batch;
class Ignore {}
//import java.io.*;
//import java.util.*;
//
//import junit.framework.*;
//
////import org.apache.bcel.classfile.*;
//import org.aspectj.ajdt.ajc.*;
////import org.aspectj.weaver.bcel.*;
//import org.aspectj.bridge.*;
//import org.aspectj.testing.util.TestUtil;
//import org.aspectj.util.*;

//public class VerifyWeaveTestCase extends WeaveTestCase {
//	{
//		regenerate = false;
//		runTests = true;
//	}
//	
//	static final String outDirName = "out";
//	static final String srcDir = "testdata" + File.separator + "src1"+ File.separator;
//
//	public VerifyWeaveTestCase(String name) {
//		super(name);
//	}
//	
//	public void testCompile() throws IOException {
//		buildTest("A", "Hello", outDirName);
//	}
//	
//	
//	public boolean doCompile(String fileToCompile, String specifiedOutDir) {
//		List args = new ArrayList();
//		if (specifiedOutDir != null) {
//			args.add("-d");
//			args.add(specifiedOutDir);
//		}
//		args.add("-classpath");
//		args.add("../runtime/bin");
//		args.add(fileToCompile);
//		
//
//		ICommand command = new AjdtCommand();
//		MessageHandler myHandler = new MessageHandler();
//	    command.runCommand((String[])args.toArray(new String[0]), myHandler);
//        IMessage[] info = myHandler.getMessages(IMessage.INFO, IMessageHolder.EQUAL);
////	    System.out.println("info messages: " + Arrays.asList(info));
////		System.out.println("compiled: " + fileToCompile);
////		System.out.println("errors: " + Arrays.asList(myHandler.getErrors()));
////		System.out.println("warnings: " + Arrays.asList(myHandler.getWarnings()));
//		return true;
//	}
//
//	public void testBuildOutputDir() throws IOException {
//		FileUtil.deleteContents(new File(outDirName));
//		
//		doCompile(srcDir + "A.java", outDirName);
//		assertTrue("default package, output dir specified", 
//			new File(outDirName + File.separator + "A.class").exists());
//	
//		File testFile = new File(srcDir + "A.class");
//		//XXX These test for javac compatible behavior with output dirs
////		testFile.delete();
////		doCompile(srcDir + "A.java", null);
////		assertTrue("default package, no output dir specified", 
////			testFile.exists());
////	
////		doCompile(srcDir + "Ap.java", null);
////		assertTrue("package named, no dir specified", 
////			new File(srcDir + "Ap.class").exists());
//	
//		doCompile(srcDir + "Ap.java", outDirName);
//		File checkFile = 
//			new File(outDirName + File.separator + "src1" + File.separator + "Ap.class");
//		assertTrue("package named, dir specified: " + checkFile.getAbsolutePath(), 
//			checkFile.exists());
//		
//	}
//
//	public void buildTest(String name, String outName, String specifiedOutDir) throws IOException {
//        String classDir = "bin";
//        
//        doCompile(srcDir  + name + ".java", specifiedOutDir);
//        
//        LazyClassGen gen = new LazyClassGen(new BcelObjectType(new ClassParser(outDirName + File.separator + outName +".class").parse()));
//
//        try {
//	        checkClass(gen, outDirName, outName + ".txt");
//			if (runTests) {
//				TestUtil.runMain(outDirName, "A");
//			}
//        } catch (Error e) {
//        	gen.print(System.err);
//        	throw e;
//        } catch (RuntimeException e) {
//        	gen.print(System.err);
//        	throw e;
//        }
//   	}
//
//}
