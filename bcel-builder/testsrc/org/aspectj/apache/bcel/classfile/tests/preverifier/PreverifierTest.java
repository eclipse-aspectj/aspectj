/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement - IBM - 3rd April 2006 - initial implementation 
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.tests.preverifier;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.apache.bcel.Repository;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ClassFormatException;
import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.StackMapFrame;
import org.aspectj.apache.bcel.classfile.StackMapTable;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.apache.bcel.verifier.utility.Frame;
import org.aspectj.apache.bcel.verifier.utility.LocalVariables;
import org.aspectj.apache.bcel.verifier.utility.OperandStack;
import org.aspectj.apache.bcel.verifier.utility.StackMapHelper;


/**
 * gotchas
 * - stack at an exception entry point - is it the stack from the try{} with the exception on?
 * - what is Top?
 * - sometimes if we haven't merged 'routes' at the start of a chain, we may commence the chain without quite
 *   the right set of types?!? do we re-stack (the start of it) and pick another and come back later?
 * - is the frame for a 'try' the frame before or after the instruction? (i.e. the one we hack for the exception handler) - ANSWER: its the 'before' but we may need to include lvar types 'learned' during the block in the catch block frame
 */

public class PreverifierTest extends PreverifierBaseTestCase {
	
	private boolean debug = true;
//	/** Check StackMapTable can be retrieved and is the right length */
//	public void testLookingAtTheStackMap() throws ClassNotFoundException {
//		StackMapTable stackmap = fetchStackMapTable("org.aspectj.apache.bcel.classfile.Utility","codeToString");
//		assertTrue("Should be 8 entries in the stack map but found: "+stackmap.getMapLength(),stackmap.getMapLength()==8);
//	}
//	
//	
//
//	/** Are the frames correct */
//    public void testTheFrames() throws ClassNotFoundException {
//		StackMapTable stackmap = fetchStackMapTable("org.aspectj.apache.bcel.classfile.Utility","codeToString");
//		StackMapFrame[] frames = stackmap.getStackMap();
//		String[] expectedKinds = new String[]{ 
//			"AppendFrame","SameFrame","SameFrame","SameFrame","SameFrameExtended",
//			"SameFrame","FullFrame","SameFrame"	
//		};
//		for (int i = 0; i < frames.length; i++) {
//			StackMapFrame frame = frames[i];
//			//System.out.println("f"+i+" is "+frame);
//			assertTrue("Frame at position "+i+" should be "+expectedKinds[i]+" but is "+frame.getKindString(),frame.getKindString().equals(expectedKinds[i]));
//		}
//    }
//    
//    
//    
//	/** Reconstructing complete frames - applying the frames to the initial */
//	public void testFrameReconstruction() throws ClassNotFoundException {
//		MethodGen method = fetchMethod("org.aspectj.apache.bcel.classfile.Utility","codeToString");
//		StackMapTable table = fetchStackMapTable(method);
//		StackMapFrame[] frames = table.getStackMap();
//		Frame[] fullFrames = StackMapper.reconstructFrames(method,table);
//		int offset = 0;
//		for (int f = 0; f < fullFrames.length; f++) {
//			StackMapFrame frame = frames[f];
//			if (f==0) offset=frame.getByteCodeOffset();
//			else offset+=1+frame.getByteCodeOffset();
//			System.out.println("Current frame (offset="+offset+") now "+fullFrames[f]);
//		}
//	}
//	
//	
//	
//	/** Creates a set of frames and also retrieves them from a method - compares them */
//	public void testFrameComparison() throws ClassNotFoundException {
//		JavaClass clazz = getClassFromJar("org.aspectj.apache.bcel.classfile.Utility");
//		Method m = getMethod(clazz,"codeToString");
//		MethodGen method = new MethodGen(m,clazz.getClassName(),new ConstantPoolGen(clazz.getConstantPool()));
//		
//		// We are comparing the full frames here, *not* the StackMapFrames
//		
//		// First retrieve them from the method attribute
//		StackMapTable table = fetchStackMapTable(method);
//		StackMapFrame[] frames = table.getStackMap();
//		Frame[] fullFramesRead       = StackMapper.reconstructFrames(method,table);
//		
//		// Now calculate them
//		long stime = System.currentTimeMillis();
//	    Frame[] fullFramesCalculated = new StackMapper(clazz).createStackMapTableAttribute(method);
//	    
//	    // tada !!! compare the bloody things
//	    assertTrue("Expected same number of frames in each case? read="+fullFramesRead.length+"  calc="+fullFramesCalculated.length,
//	    		fullFramesRead.length==fullFramesCalculated.length);
//	    
//	    int pos = 0;
//	    for (int i = 0; i < fullFramesCalculated.length; i++) {
//	    	    if (i==0)   pos=frames[i].getByteCodeOffset();
//	    	    else      pos+=frames[i].getByteCodeOffset()+1;
//	    	    assertTrue("Offset "+pos+" frames should be equal? ["+i+"] "+differences(fullFramesRead[i],fullFramesCalculated[i])+
//	    	    		      "\n READ="+fullFramesRead[i]+"\n CALC="+fullFramesCalculated[i],
//	    	    									   equivalent(fullFramesRead[i],fullFramesCalculated[i]));
//		}
//	}
//	
//	private boolean debug = true;
//	/** 
//	 * Creates a set of frames and also retrieves them from a method - compares them.
//	 * From the 'test a load of stuff' tests, individual problematic methods are promoted to having their own testcase
//	 *  
//	 *  Fun facts:
//	 *  ChopFrame occurs in this one!
//	 */
//	public void testFrameComparison2() throws ClassNotFoundException {
//		JavaClass clazz = getClassFromJar("org.aspectj.apache.bcel.classfile.Utility");
//		Method m = getMethod(clazz,"methodTypeToSignature");
//		MethodGen method = new MethodGen(m,clazz.getClassName(),new ConstantPoolGen(clazz.getConstantPool()));
//		
//		// We are comparing the full frames here, *not* the StackMapFrames
//		
//		// First retrieve them from the method attribute
//		StackMapTable table = fetchStackMapTable(method);
//		StackMapFrame[] frames = table.getStackMap();
//		Frame[] fullFramesRead       = StackMapper.reconstructFrames(method,table);
//		if (debug) {
//			System.err.println("READ FRAMES - START");
//			for (int i = 0; i < fullFramesRead.length; i++) {
//				Frame frame = fullFramesRead[i];
//				System.err.println(fullFramesRead[i]);
//			}
//			System.err.println("READ FRAMES - END");
//		}
//		// Now calculate them
//		long stime = System.currentTimeMillis();
//	    Frame[] fullFramesCalculated = new StackMapper(clazz).createStackMapTableAttribute(method);
//	    if (debug) {
//			System.err.println("CALC FRAMES - START");
//			for (int i = 0; i < fullFramesCalculated.length; i++) {
//				Frame frame = fullFramesCalculated[i];
//				System.err.println(fullFramesCalculated[i]);
//			}
//			System.err.println("CALC FRAMES - END");
//		}
//	    
//	    // tada !!! compare the bloody things
//	    assertTrue("Expected same number of frames in each case? read="+fullFramesRead.length+"  calc="+fullFramesCalculated.length,
//	    		fullFramesRead.length==fullFramesCalculated.length);
//	    
//	    int pos = 0;
//	    for (int i = 0; i < fullFramesCalculated.length; i++) {
//	    	    if (i==0)   pos=frames[i].getByteCodeOffset();
//	    	    else      pos+=frames[i].getByteCodeOffset()+1;
//	    	    assertTrue("Offset "+pos+" frames should be equal? ["+i+"] "+differences(fullFramesRead[i],fullFramesCalculated[i])+
//	    	    		      "\n READ="+fullFramesRead[i]+"\n CALC="+fullFramesCalculated[i],
//	    	    									   equivalent(fullFramesRead[i],fullFramesCalculated[i]));
//		}
//	}
	
	private int[] toOffsets(StackMapFrame[] smfs) {
		int[] offsets = new int[smfs.length+1];
		int idx=0;
		offsets[idx++]=0;
		for (int i = 0; i < smfs.length; i++) {
			offsets[idx]=offsets[idx-1]+smfs[i].getByteCodeOffset()+(i>0?1:0);
			idx++;
		}
		return offsets;
	}
	

	public void testSimpleFrameComparison() throws ClassNotFoundException, ClassFormatException, IOException {
		String cname = "Code";
		String mname = "method1";

		JavaClass clazz = getClassFromJar(cname);
		Method interestingMethod = getMethod(clazz,mname,0);
		System.out.println(interestingMethod);
		
		MethodGen method = new MethodGen(interestingMethod,clazz.getClassName(),new ConstantPoolGen(clazz.getConstantPool()));
		
		// We are comparing the full frames here, *not* the StackMapFrames
		
		// First retrieve them from the method attribute - this is what the COMPILER generated
		StackMapTable   table  = fetchStackMapTable(method);
		StackMapFrame[] frames = table.getStackMap();
		int[]          offsets = toOffsets(frames);
		Frame[] compilerFrames = StackMapHelper.reconstructFrames(method,table);
		if (debug) printFrames("Javac",compilerFrames,offsets);
		if (true) return;
		
		// Now calculate them
		long stime = System.currentTimeMillis();
	   // Frame[] fullFramesCalculated = new StackMapper(clazz).createStackMapTableAttribute(method);
//	    Frame[] calculatedFrames = new StackMapper(clazz).produceStackMapTableAttribute(clazz, method);
//	    if (debug) printFrames("Calculated",calculatedFrames,offsets);
	 
//	    compareFrames("Compiler and Bcel frames",compilerFrames,calculatedFrames,frames);
	}
	public void testComplexFrameComparison() throws ClassNotFoundException, ClassFormatException, IOException {
		String cname = "org.aspectj.apache.bcel.classfile.Utility";
		String mname = "methodSignatureToString";
		int count = 2;

		JavaClass clazz = getClassFromJar(cname);
		ClassGen cg = new ClassGen(clazz);
		Method interestingMethod = getMethod(cg,mname,2);
		System.out.println(interestingMethod);

		MethodGen method = new MethodGen(interestingMethod,clazz.getClassName(),new ConstantPoolGen(clazz.getConstantPool()));
		method.calculateStackMapTable();
		cg.replaceMethod(interestingMethod, method.getMethod());

		// method.calculateStackMapTable();
		String path = "c:/temp/"+clazz.getPackageName().replaceAll("\\.","/");
		String name = clazz.getClassName();
		if (name.indexOf(".")!=-1) {
			name = name.substring(name.lastIndexOf(".")+1);
		}
		String file = path+"/"+name+".class";
		System.err.println(file);
		new File(path).mkdirs();
		cg.getJavaClass().dump(new FileOutputStream(file));
		URLClassLoader cl = new URLClassLoader(new URL[]{new File("c:/temp").toURL(),new File("c:/testcode.jar").toURL()},null);//this.getClass().getClassLoader());
		System.err.println(cl.getURLs()[0]);
		System.err.println("loading "+clazz.getClassName());
		Class c = Class.forName(clazz.getClassName(),true,cl);
//		
//		// First retrieve them from the method attribute - this is what the COMPILER generated
//		StackMapTable   table  = fetchStackMapTable(method);
//		StackMapFrame[] frames = table.getStackMap();
//		int[]          offsets = toOffsets(frames);
//		Frame[] compilerFrames = StackMapper.reconstructFrames(method,table);
//		if (debug) printFrames("Javac",compilerFrames,offsets);
//		
//		// Now calculate them
//		long stime = System.currentTimeMillis();
//	    Frame[] fullFramesCalculated = new StackMapper(clazz).createStackMapTableAttribute(method);
//	    Frame[] calculatedFrames = new StackMapper(clazz).produceStackMapTableAttribute(clazz, method);
////	    if (debug) printFrames("Calculated",calculatedFrames,offsets);
////	 
////	    compareFrames("Compiler and Bcel frames",compilerFrames,calculatedFrames,frames);
////	    
////	    // let's try serializing the new version of the method with this on...
////	    method.addStackMap(calculatedFrames);
	    
	}
	

	public void testProblemClassOne() throws ClassNotFoundException, ClassFormatException, IOException {
		// Uncovered two problems surfaced: 
		// - Bcel not understanding LDC changes to support Class Constants in the ExecutionVisitor
		// - Bcel 'optimizing' wide instructions that don't use double byte indices - the stackmap is left unchanged and so incorrect
		checkOneClass("org.aspectj.apache.bcel.generic.InstructionHandle",true,3);
	}
	
	public void testProblemClassTwo() throws ClassNotFoundException, ClassFormatException, IOException {
		// simple problem revealed in test system with not avoiding abstract methods
		checkOneClass("org.aspectj.apache.bcel.classfile.annotation.ElementValue",true,3);
	}
	
	public void testProblemClassThree() throws ClassNotFoundException, ClassFormatException, IOException {
		// problem in deserializing a stackmaptable - incorrectly used the localcount when creating the stack array !
		// problem with uninitialized - where the index should be the index of the related 'new' instruction
		// - enhanced execution visitor to remember where it is
		// problem with methods that have no stackmap (readVersion in this class)
		// lots of problems where 'unknown' was being used when it should have been 'top'
		checkOneClass("org.aspectj.apache.bcel.classfile.ClassParser",true,3);
	}
	
	public void testEveryMethodInAClass() throws ClassNotFoundException, ClassFormatException, IOException {
		checkOneClass("org.aspectj.apache.bcel.classfile.Utility",false);
	}
	
	public void testProblemClassFour() throws ClassNotFoundException, ClassFormatException, IOException {
		// Problem with incorrect handling of Type.NULL - it is a special ObjectType constant
		checkOneClass("org.aspectj.apache.bcel.classfile.GenericSignatureParser",true);
	}

	public void testProblemClassFive() throws ClassNotFoundException, ClassFormatException, IOException {
		// outofmemory on the clinit... it is 8870 bytes long
		// ended up fixing the exceptionhandler creation logic - so it doesnt build a list for every instruction
		// of the exceptions that might occur, just for those instructions that actually throw exceptions!
		long timer = System.currentTimeMillis();
		checkOneClass("org.aspectj.apache.bcel.Constants",true);
		System.out.println("took "+(System.currentTimeMillis()-timer)+"ms");
		// 3926/2684ms! 7/12/06
	}
	
	public void testProblemClassSix() throws ClassNotFoundException, ClassFormatException, IOException {
		// Problem here was that after optimizing the exception code so that it didn't persue so many execution
		// chains, i forgot that astores alter the local variable table for the catch block - and in this
		// case, if I forgot that the catch block thought LV1 was <null> when in fact it was Object
		// fix is to be a little less aggressive - and recognize ASTOREs need to be followed through the
		// catch block
		checkOneClass("org.aspectj.apache.bcel.classfile.Attribute",true,14); // clone method
	}
	
	public void testProblemClassSeven() throws ClassNotFoundException, ClassFormatException, IOException {
		// Now we had a problem with double slot values (long/double) - they are internally represented but when
		// the frame is dumped the duplicate TOPs are removed.
		// Changes were to StackMapHelper.createInitialFrame() and StackMapFrame.dump() and Frame.getLocalsAsStackMapTypes()
		checkOneClass("org.aspectj.apache.bcel.generic.ConstantPoolGen",true,17);
	}
	
	public void testProblemClassEight() throws ClassNotFoundException, ClassFormatException, IOException {
		// java.lang.VerifyError: Instruction type does not match stack map in method org.aspectj.apache.bcel.verifier.utility.StackMapper.chaseChains()V at offset 0
		// interesting!  The method code ends with a 'goto 0' causing us to jump back to the start - because I didn't store the initialframe for the
		// method, we didn't do a merge after the jump back to 0, we just used what the contents were.  A merge would have told us we knew much
		// less than we thought.  The fix was to store the initial frame as we start
		checkOneClass("org.aspectj.apache.bcel.verifier.utility.StackMapper",true,11);
	}
	
	// rt.jar classes
	public void testProblemClass9() throws ClassNotFoundException, ClassFormatException, IOException {
		// Revealed that the OperandStack wasn't correctly working for double slot entries like LONG
		checkOneClass("com.sun.corba.se.impl.corba.AnyImpl",true,34);
	}
	
	public void testProblemClass10() throws ClassNotFoundException, ClassFormatException, IOException {
		// method 2 - internalGetServant has jsrs, yey!
		checkOneClass("com.sun.corba.se.impl.oa.poa.POAPolicyMediatorImpl_R_USM",true,2);
	}
	
	public void testProblemClass11() throws ClassNotFoundException, ClassFormatException, IOException {
		checkOneClass("com.sun.corba.se.impl.corba.AnyImplHelper",false);
	}
	
	
	public void testJar() throws ClassNotFoundException, ClassFormatException, IOException {
		StackMapHelper.count=0;
		verifyJar("testdata/Java6BuiltCode.jar");
		System.out.println("Stored: "+StackMapHelper.count+" frames");
	}
	
	public void testJar2() throws ClassNotFoundException, ClassFormatException, IOException {
		verifyJar("C:/Program Files/Java/jdk1.6.0/jre/lib/rt.jar");
	}
	
	public void verifyJar(String where) throws ClassNotFoundException, ClassFormatException, IOException {
		long timer   = System.currentTimeMillis();
		ZipFile zf    = new ZipFile(where);
		Enumeration e = zf.entries();
		int count=0,verified=0;
		while (e.hasMoreElements()) {
			ZipEntry zfe = (ZipEntry)e.nextElement();
			if (zfe.getName().endsWith(".class")) {
				String n = zfe.getName();
				if (debug) System.out.println("Processing class #"+count+": "+n);
				n = n.substring(0,n.indexOf(".class"));
				if (n.indexOf("OperatingSystem")==-1 && n.indexOf("JdbcOdbc")==-1) {
					boolean b = checkOneClass(n.replaceAll("/","."),false);
					count++;
					if (b) verified++;
				}
			}
		}
		System.out.println("Tested entire jar, took "+(System.currentTimeMillis()-timer)+"ms for "+count+" classes (verified: "+verified+")");
	}
	
	
	public File getTempDir() {
		String s = "c:/temp";//System.getProperty("java.io.tmpdir");
		File f = new File(s+File.separator+"verifier");
		f.mkdir();
		return f;
	}
	
	public boolean checkOneClass(String cname,boolean verifyAsYouGoAlong) throws ClassNotFoundException, ClassFormatException, IOException {
		return checkOneClass(cname,verifyAsYouGoAlong,0);
	}
	
//	public static void main(String []argv) throws ClassNotFoundException, FileNotFoundException, IOException {
//		// remove one 
//		ClassPath cp = new ClassPath(
//				"c:/igor/fails/"+File.pathSeparator+
//				System.getProperty("sun.boot.class.path"));
//		SyntheticRepository repos =  SyntheticRepository.getInstance(cp);
//		Repository.setRepository(repos);
//		JavaClass clazz = repos.loadClass("com.ibm.tivoli.itcam.toolkit.ai.aspectj.captureJDBC.CaptureDataSource");
//		ClassGen cg = new ClassGen(clazz);
//		Attributes[] as = cg.getAttributes();
//		for (int i = 0; i < as.length; i++) {
//			
//		}
//		File f = new File("c:/igor/CaptureDataSource.class");
//		cg.getJavaClass().dump(new FileOutputStream(f));
//		
//	}
	
	public boolean checkOneClass(String cname,boolean verifyAsYouGoAlong,int startMethod) throws ClassNotFoundException, ClassFormatException, IOException {
		if (debug) System.out.println("? PreverifierTest.testClass(): testing class '"+cname+"'");
		long timer = System.currentTimeMillis();
		JavaClass clazz = getClassFromJar(cname);
		ClassGen cg = new ClassGen(clazz);
		cg.setMajor(50); // upgrade it so it gets verified!
//		if (cg.getMajor()<50) {
//			System.out.println("? skipping this class, it is major version "+cg.getMajor());
//			return false;
//		}
		
		File tempDir = getTempDir();
		String destination = clazz.getPackageName().replaceAll("\\.","/");		
		String name = clazz.getClassName();
		if (name.indexOf(".")!=-1) name = name.substring(name.lastIndexOf(".")+1);
		File outputDir = new File(tempDir,destination);
		outputDir.mkdirs();
		File file = new File (outputDir,name+".class");
		if (debug) System.out.println("? New file will be dumped to "+file);
		Method[] methods = cg.getMethods();
		for (int i = startMethod; i <methods.length; i++) {
			Method originalMethod = methods[i];
			if (originalMethod.isAbstract()) {
				if (debug) System.out.println("? skipping abstract method "+i+"/"+methods.length);
			} else {
				if (debug) System.out.println("? processing method "+i+"/"+methods.length+": "+originalMethod.getName());
				ConstantPoolGen cpg = cg.getConstantPool();
				MethodGen newMethod = new MethodGen(originalMethod,clazz.getClassName(),cpg);
				newMethod.calculateStackMapTable();
				cg.replaceMethod(originalMethod, newMethod.getMethod());
			
				if (verifyAsYouGoAlong) {
					cg.getJavaClass().dump(new FileOutputStream(file));
					URLClassLoader cl = new URLClassLoader(new URL[]{tempDir.toURL(),new File("c:/testcode.jar").toURL()},null);//this.getClass().getClassLoader());
					Class c = Class.forName(clazz.getClassName(),true,cl);
				}
			}
	 
		}
		if (!verifyAsYouGoAlong) { // Just do this once at the end
			cg.getJavaClass().dump(new FileOutputStream(file));
			URLClassLoader cl = new URLClassLoader(new URL[]{tempDir.toURL(),new File("c:/testcode.jar").toURL()},null);//this.getClass().getClassLoader());
			Class c = Class.forName(clazz.getClassName(),true,cl);
		}
		if (debug) System.out.println("Time taken testing "+cname+": "+(System.currentTimeMillis()-timer)+"ms");
		return true;
	}
	
	
	
	private void printFrames(String id,Frame[] fs,int[] offsets) {
		System.err.println(id+": start ("+fs.length+" frames)");
		for (int i = 0; i < fs.length; i++) System.err.println(fs[i].toString(i+"(o"+offsets[i]+")"));
		System.err.println(id+": end");		
	}
    
	/** Compare two sets of frames - the final parameter is just used for offset reporting*/
	public void compareFrames(String description, Frame[] compilerFrames,Frame[] calculatedFrames,StackMapFrame[] frames) {
		System.out.println(description);
	    assertTrue("Expected same number of frames in each case? read="+compilerFrames.length+"  calc="+calculatedFrames.length,
	    		compilerFrames.length==calculatedFrames.length);
	    
	    int pos = 0;
	    for (int i = 0; i < calculatedFrames.length; i++) {
			System.out.println("Comparing frames at offset "+pos);
		    assertTrue("Offset "+pos+" frames should be equal? ["+i+"] "+differences(compilerFrames[i],calculatedFrames[i])+
		    		      "\n READ="+compilerFrames[i]+"\n CALC="+calculatedFrames[i],equivalent(compilerFrames[i],calculatedFrames[i]));
		    if (i<calculatedFrames.length-1) pos+=frames[i].getByteCodeOffset()+(i>0?1:0);
		}
	    System.out.println("comparison successful");
	}
	
	
	
	/** 
	 * Creates a set of frames and also retrieves them from a method - compares them.
	 *  
	 *  Victim: org.aspectj.apache.bcel.classfile.Utility.methodSignatureToString()  (the 2nd one)
	 */
//	public void testFrameComparison() throws ClassNotFoundException, ClassFormatException, IOException {
//		JavaClass clazz = getClassFromJar("org.aspectj.apache.bcel.classfile.Utility");
//		Method interestingMethod = getMethod(clazz,"methodSignatureToString",2);
//		System.out.println(interestingMethod);
//		
//		MethodGen method = new MethodGen(interestingMethod,clazz.getClassName(),new ConstantPoolGen(clazz.getConstantPool()));
//		
//		// We are comparing the full frames here, *not* the StackMapFrames
//		
//		
//		// First retrieve them from the method attribute - this is what the COMPILER generated
//		StackMapTable   table  = fetchStackMapTable(method);
//		StackMapFrame[] frames = table.getStackMap();
//		Frame[] fullFrames = StackMapper.reconstructFrames(method,table);
//		if (debug) {
//			System.out.println("Javac: start ("+fullFrames.length+" frames)");
//			for (int i = 0; i < fullFrames.length; i++) System.out.println(fullFrames[i].toString(i+") "));
//			System.out.println("Javac: end");
//		}
//		
//		
//		
//		
//		
//		// Now calculate them
//		long stime = System.currentTimeMillis();
//	   // Frame[] fullFramesCalculated = new StackMapper(clazz).createStackMapTableAttribute(method);
//	    Frame[] fullFramesCalculated = new StackMapper(clazz).produceStackMapTableAttribute(clazz, method);//(method);
//	    if (debug) {
//			System.out.println("Calculated: start ("+fullFramesCalculated.length+" frames)");
//			for (int i = 0; i < fullFramesCalculated.length; i++)
//				System.out.println(fullFramesCalculated[i]);
//			System.out.println("Calculated: end");
//		}
//	    
//	    // tada !!! compare the bloody things
////	    assertTrue("Expected same number of frames in each case? read="+fullFramesRead.length+"  calc="+fullFramesCalculated.length,
////	    		fullFramesRead.length==fullFramesCalculated.length);
//	    
//	    int pos = 0;
//	    for (int i = 0; i < fullFramesCalculated.length; i++) {
//	    	    if (i==0)   pos=frames[i].getByteCodeOffset();
//	    	    else      pos+=frames[i].getByteCodeOffset()+1;
//	    	    assertTrue("Offset "+pos+" frames should be equal? ["+i+"] "+differences(fullFrames[i],fullFramesCalculated[i])+
//	    	    		      "\n READ="+fullFrames[i]+"\n CALC="+fullFramesCalculated[i],
//	    	    									   equivalent(fullFrames[i],fullFramesCalculated[i]));
//		}
//	}

	
//	public void testTheChuffinLot() throws ClassNotFoundException {
//		// Load a whole class and test all its frames for every method.
//		int compared=0;
//		JavaClass clazz = getClassFromJar("org.aspectj.apache.bcel.classfile.Utility");
//		ClassGen cg = new ClassGen(clazz);
//		Method[] ms = cg.getMethods();
//		System.err.println("Class "+clazz.getClassName()+" has "+ms.length+" methods to compare");
//		for (int i = 0; i < ms.length; i++) {
//			Method m = ms[i];
//			MethodGen method = new MethodGen(m,clazz.getClassName(),cg.getConstantPool());
//			if (!hasStackMapTableAttribute(method)) {System.err.println("No StackMapAttribute for "+method.getName());continue;}
//			System.err.println("====================================================");
//			System.err.println("Method "+i+" is '"+m+"': commencing frame comparison");
//			// First retrieve them from the method attribute
//			StackMapTable table = fetchStackMapTable(method);
//			StackMapFrame[] frames = table.getStackMap();
//			Frame[] fullFramesRead       = StackMapper.reconstructFrames(method,table);
//			
//			// Now calculate them
//			long stime = System.currentTimeMillis();
//		    Frame[] fullFramesCalculated = new StackMapper(clazz).createStackMapTableAttribute(method);
//		    
//		    // tada !!! compare the bloody things
//		    assertTrue("Expected same number of frames in each case? read="+fullFramesRead.length+"  calc="+fullFramesCalculated.length,
//		    		fullFramesRead.length==fullFramesCalculated.length);
//		    
//		    int pos = 0;
//		    for (int ii = 0; ii < fullFramesCalculated.length; ii++) {
//		    	    if (ii==0)   pos=frames[ii].getByteCodeOffset();
//		    	    else      pos+=frames[ii].getByteCodeOffset()+1;
//		    	    assertTrue("Offset "+pos+" frames should be equal? ["+ii+"] "+differences(fullFramesRead[ii],fullFramesCalculated[ii])+
//		    	    		      "\n READ="+fullFramesRead[ii]+"\n CALC="+fullFramesCalculated[ii],
//		    	    									   equivalent(fullFramesRead[ii],fullFramesCalculated[ii]));
//			}
//		    compared++;
//		}
//		System.err.println("Successfully compared attributes for "+compared+" methods");
//	}
		
//	ZipFile zf = new ZipFile(f);
//	int i = 0;
//	long stime = System.currentTimeMillis();
//	Enumeration entries = zf.entries();
//	while (entries.hasMoreElements()) {
//		ZipEntry zfe = (ZipEntry) entries.nextElement();
//		String classfileName = zfe.getName();
//		if (classfileName.endsWith(".class")) {
//			String clazzname = classfileName.substring(0,
//					classfileName.length() - 6).replace('/', '.');
//			ReferenceType b = (ReferenceType) slowWorld
//					.resolve(clazzname);
//			i++;
//		}
//	}
	
	
//	public void testDumbVerifier() throws ClassNotFoundException {
//		MethodGen method = fetchMethod("org.aspectj.apache.bcel.classfile.Utility","codeToString");
//		
//		JavaClass clazz = getClassFromJar("org.aspectj.apache.bcel.classfile.Utility");
//		Method m = getMethod(clazz,"codeToString");
//		Attribute a = findAttribute("Code", m.getAttributes());
//		assertTrue("Should be of type 'Code' but is "+a.getClass(),a instanceof Code);
//		Code c = (Code)a;
//		Attribute[] codeAttributes = c.getAttributes();
//		StackMapTable stackmap = (StackMapTable)findAttribute("StackMapTable", c.getAttributes());
//		assertTrue("Should be 8 entries in the stack map but found: "+stackmap.getMapLength(),stackmap.getMapLength()==8);
//		try {
//			MethodGen mg = new MethodGen(m,clazz.getClassName(),new ConstantPoolGen(clazz.getConstantPool()));
//			long stime = System.currentTimeMillis();
//		   new StackMapper().createStackMapTableAttribute(mg);
//		   System.err.println("Took "+(System.currentTimeMillis()-stime)+"ms");
//		} catch (Exception e) {e.printStackTrace();}
//	}
	
	/**
	 * Retrieves a stack map then asks for verification of the method in the class which spits out
	 * stackmapframes we can compare against...
	 * this checks:
	 * - the verifier works
	 * - the stackmap loading is correct
	 * - the process for having our own cut down verifier
	 */
	
//	
//	
//	
//	public void testVerifyTheMap() throws ClassNotFoundException {
////		JavaClass clazz = getClassFromJar("org.aspectj.apache.bcel.classfile.Utility");
////		Method m = getMethod(clazz,"codeToString");
//		JavaClass clazz = getClassFromJar("org.aspectj.apache.bcel.classfile.Utility");
//		Method m = getMethod(clazz,"methodSignatureToString",2);
//		Attribute a = findAttribute("Code", m.getAttributes());
//		assertTrue("Should be of type 'Code' but is "+a.getClass(),a instanceof Code);
//		Code c = (Code)a;
//		Attribute[] codeAttributes = c.getAttributes();
//		StackMapTable stackmap = (StackMapTable)findAttribute("StackMapTable", c.getAttributes());
////		assertTrue("Should be 8 entries in the stack map but found: "+stackmap.getMapLength(),stackmap.getMapLength()==8);
//		try {
//			MethodGen mg = new MethodGen(m,clazz.getClassName(),new ConstantPoolGen(clazz.getConstantPool()));
//			long stime = System.currentTimeMillis();
//			Frame[] o = new StackMapper(clazz).produceStackMapTableAttribute(clazz,mg);
//			System.err.println("Took "+(System.currentTimeMillis()-stime)+"ms");
//		} catch (Exception e) {e.printStackTrace();}
//	}
//	
//	
//	
//	/** Let's test the creation of subroutines for a method */
//	public void testSubroutines() throws ClassNotFoundException {
//		JavaClass clazz = getClassFromJar("org.aspectj.apache.bcel.classfile.Utility");
//		Method m = getMethod(clazz,"methodSignatureToString",2);
//		MethodGen mg = new MethodGen(m,clazz.getClassName(),new ConstantPoolGen(clazz.getConstantPool()));
//		long stime = System.currentTimeMillis();
//		Subroutines subs = new Subroutines(mg);
//		System.err.println("Took "+(System.currentTimeMillis()-stime)+"ms");
//		Collection c = subs.getIndividualSubroutines();
//		System.err.println("End of analysis: "+c.size()+" subroutines");
//		for (Iterator iter = c.iterator(); iter.hasNext();) {
//			Subroutine element = (Subroutine) iter.next();
//			System.err.println(element);
//		}
//	}
//
//

	// ---
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// ---
//	private StackMapTable fetchStackMapTable(String classname,String methodname) throws ClassNotFoundException {
//		JavaClass clazz = getClassFromJar(classname);
//		Method m = getMethod(clazz,methodname);
//		Attribute a = findAttribute("Code", m.getAttributes());
//		Code c = (Code)a;
//		Attribute[] codeAttributes = c.getAttributes();
//		CodeException[] ces = c.getExceptionTable();
//		StackMapTable stackmap = (StackMapTable)findAttribute("StackMapTable", c.getAttributes());
//		return stackmap;
//	}
//	
//	private boolean hasStackMapTableAttribute(MethodGen method) {
//		Attribute[] all = method.getCodeAttributes();
//		for (int i = 0; i < all.length; i++) {
//			if (all[i].getName().equals("StackMapTable")) return true;
//		}
//		return false;
//	}

	private StackMapTable fetchStackMapTable(MethodGen method) throws ClassNotFoundException {
		Attribute[] codeAttributes = method.getCodeAttributes();
		StackMapTable stackmap = (StackMapTable)findAttribute("StackMapTable", codeAttributes);
		return stackmap;
	}
	
	private MethodGen fetchMethod(String classname,String methodname) throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar(classname);
		Method m = getMethod(clazz,methodname);
		MethodGen mg = new MethodGen(m,clazz.getClassName(),new ConstantPoolGen(clazz.getConstantPool()));
		return mg;
	}


	/** For frames that aren't the same (equivalent()) this method will return why */
	private String differences(Frame f1,Frame f2) {
		LocalVariables f1Locals = f1.getLocals();
		LocalVariables f2Locals = f2.getLocals();
		OperandStack f1Stack = f1.getStack();
		OperandStack f2Stack = f2.getStack();
		
		if (f1Locals.maxLocals()!=f2Locals.maxLocals()) return "Different number of locals";
		if (f1Stack.maxStack()!=f2Stack.maxStack()) return "Different max stack sizes";
		if (f1Stack.size()!=f2Stack.size()) return "Difference 'current' stack depths";

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < f1Locals.maxLocals(); i++) {
			Type f1Type = f1Locals.get(i);
			Type f2Type = f2Locals.get(i);
			if (!f1Type.equals(f2Type)) return "Different types at local variable position "+i+"  "+f1Type+"!="+f2Type;	
		}
		for (int i = 0; i < f1Stack.size(); i++) {
			Type f1Type = f1Stack.peek(i);
			Type f2Type = f2Stack.peek(i);
			if (!f1Type.equals(f2Type)) return "Different types at stack depth "+i+"  "+f1Type+"!="+f2Type;			
		}
		return "";
	}
	
	/** Are two frames the same? */
	private boolean equivalent(Frame f1,Frame f2) {
		LocalVariables f1Locals = f1.getLocals();
		LocalVariables f2Locals = f2.getLocals();
		OperandStack f1Stack = f1.getStack();
		OperandStack f2Stack = f2.getStack();
		
		if (f1Locals.maxLocals()!=f2Locals.maxLocals()) return false;
		if (f1Stack.maxStack()!=f2Stack.maxStack()) return false;
		if (f1Stack.size()!=f2Stack.size()) return false;

		for (int i = 0; i < f1Locals.maxLocals(); i++) {
			Type f1Type = f1Locals.get(i);
			Type f2Type = f2Locals.get(i);
			if (!f1Type.equals(f2Type)) return false;			
		}
		for (int i = 0; i < f1Stack.size(); i++) {
			Type f1Type = f1Stack.peek(i);
			Type f2Type = f2Stack.peek(i);
			if (!f1Type.equals(f2Type)) return false;			
		}
		return true;
	}

	private void dump(Attribute[] mAttributes) {
		for (int i = 0; i < mAttributes.length; i++) {
			Attribute attribute = mAttributes[i];
			System.err.println(attribute);
		}
	}
	
}
