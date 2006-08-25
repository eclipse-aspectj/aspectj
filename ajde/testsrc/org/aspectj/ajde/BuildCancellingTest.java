/* *******************************************************************
 * Copyright (c) 2004 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *    Andy Clement     Initial version
 * ******************************************************************/

package org.aspectj.ajde;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.internal.CompilerAdapter;
import org.aspectj.util.FileUtil;

/**
 * It is now possible to cancel the compiler during either the
 * compilation or weaving phases - this testcase verifies a few
 * cases, making sure the process stops when expected.  It can
 * check the disk contents, but it doesn't right now.
 * 
 * Two different .lst files are used during these tests: LoadsaCode.lst and 
 * EvenMoreCode.lst which contain mixes of aspects and classes
 * 
 * Here are some things to think about that will help you understand what is 
 * on the disk when we cancel the compiler.
 * 
 * There are 3 important phases worth remembering :
 * - Compile all the types 
 * - Weave all the aspects
 * - Weave all the classes
 * 
 * Each of those steps goes through all the types.  This
 * means during the 'weave all the aspects' step we are 
 * jumping over classes and during the 'weave all the
 * classes ' step we are jumping over aspects.  Why is this important?
 * 
 *  
 * We only write bytes out during the 'weave all the classes ' phase and it is even
 * during that phase that we write out the bytes for aspects.  This means if you cancel
 * during compilation or during the weaving of aspects - there will be nothing on the 
 * disk.  If you cancel whilst in the 'weave all the classes ' phase then the disk
 * will contain anything finished with by the cancellation point.
 */
public class BuildCancellingTest extends AjdeTestCase {

	private CompilerAdapter compilerAdapter;
	public static final String PROJECT_DIR = "BuildCancelling";
	public static final String binDir = "bin";
	private static final boolean debugTests = false;

	public BuildCancellingTest(String arg0) {
		super(arg0);
	}

	// Ensure the output directory is clean
	protected void setUp() throws Exception {
		super.setUp(PROJECT_DIR);
		FileUtil.deleteContents(openFile(binDir));
	}
	

	
	/**
	 * After first compilation message, get it to cancel, there should be one more warning
	 * message about cancelling the compile and their should be nothing on the disk.
	 */
	public void testCancelFirstCompile() {
		if (debugTests) System.out.println("\n\n\ntestCancelFirstCompile: Building with LoadsaCode.lst");
		compilerAdapter = new CompilerAdapter();
		BuildProgMon programmableBPM = new BuildProgMon();

		programmableBPM.cancelOn("compiled:",1); // Force a cancel after the first compile occurs

		compilerAdapter.compile(
			(String) openFile("LoadsaCode.lst").getAbsolutePath(),
			programmableBPM,
			false);
			
		assertTrue("Should have cancelled after first compile?:"+programmableBPM.numCompiledMessages,
		  programmableBPM.numCompiledMessages==1);
		
// Comment out to check the disk contents  
//		assertTrue("As weaving was cancelled, no files should have been written out, but I found:"+wovenClassesFound(),
//		  wovenClassesFound()==0);
		  
		boolean expectedCancelMessageFound = checkFor("Compilation cancelled as requested");
		if (!expectedCancelMessageFound) dumpTaskData(); // Useful for debugging
		assertTrue("Failed to get warning message about compilation being cancelled!", expectedCancelMessageFound);
	}
	
	
	
	/**
	 * After third compilation message, get it to cancel, there should be one more warning
	 * message about cancelling the compile and their should be nothing on the disk.
	 */
	public void testCancelThirdCompile() {
		if (debugTests) System.out.println("\n\n\ntestCancelThirdCompile: Building with LoadsaCode.lst");
		compilerAdapter = new CompilerAdapter();
		BuildProgMon programmableBPM = new BuildProgMon();

		programmableBPM.cancelOn("compiled:",3); // Force a cancel after the third compile occurs

		compilerAdapter.compile(
			(String) openFile("LoadsaCode.lst").getAbsolutePath(),
			programmableBPM,
			false);
			
		assertTrue("Should have cancelled after third compile?:"+programmableBPM.numCompiledMessages,
		  programmableBPM.numCompiledMessages==3);
		  
//		Comment out to check the disk contents 		  
//		assertTrue("As weaving was cancelled, no files should have been written out, but I found:"+wovenClassesFound(),
//		  wovenClassesFound()==0);

		boolean expectedCancelMessageFound = checkFor("Compilation cancelled as requested");
		if (!expectedCancelMessageFound) dumpTaskData(); // Useful for debugging
		assertTrue("Failed to get warning message about compilation being cancelled!", expectedCancelMessageFound);
	}


	/**
	 * After first weave aspect message, get it to cancel, there should be one more warning
	 * message about cancelling the weave and their should be nothing on the disk.
	 */
	public void testCancelFirstAspectWeave() {
		if (debugTests) System.out.println("\n\n\ntestCancelFirstAspectWeave: Building with LoadsaCode.lst");
		compilerAdapter = new CompilerAdapter();
		BuildProgMon programmableBPM = new BuildProgMon();

		programmableBPM.cancelOn("woven aspect ",1); // Force a cancel after the first weave aspect occurs

		compilerAdapter.compile((String) openFile("LoadsaCode.lst").getAbsolutePath(),programmableBPM,false);
			
		assertTrue("Should have cancelled after first aspect weave?:"+programmableBPM.numWovenAspectMessages,
		  programmableBPM.numWovenAspectMessages==1);

// 		Comment out to check the disk contents
//		assertTrue("As weaving was cancelled, no files should have been written out?:"+wovenClassesFound(),
//		  wovenClassesFound()==0);

		boolean expectedCancelMessageFound = checkFor("Weaving cancelled as requested");
		if (!expectedCancelMessageFound) dumpTaskData(); // Useful for debugging
		assertTrue("Failed to get warning message about weaving being cancelled!", expectedCancelMessageFound);
	}
	


	/**
	 * After third weave aspect message, get it to cancel, there should be one more warning
	 * message about cancelling the weave and their should be nothing on the disk.
	 */	
	public void testCancelThirdAspectWeave() {
		if (debugTests) System.out.println("\n\n\ntestCancelThirdAspectWeave: Building with LoadsaCode.lst");
		compilerAdapter = new CompilerAdapter();
		
		BuildProgMon programmableBPM = new BuildProgMon();
		// Force a cancel after the third weave occurs.
		// This should leave two class files on disk - I think?
		programmableBPM.cancelOn("woven aspect ",3); 
			
		compilerAdapter.compile(
			(String) openFile("LoadsaCode.lst").getAbsolutePath(),
			programmableBPM,
			false);
		
		assertTrue("Should have cancelled after third weave?:"+programmableBPM.numWovenAspectMessages,
		  programmableBPM.numWovenAspectMessages==3);
		  
//		Comment out to check disk contents
//		assertTrue("As weaving was cancelled, no files should have been written out?:"+wovenClassesFound(),
//		  wovenClassesFound()==0);
		  
		boolean expectedCancelMessageFound = checkFor("Weaving cancelled as requested");
		if (!expectedCancelMessageFound) dumpTaskData(); // Useful for debugging
		assertTrue("Failed to get warning message about weaving being cancelled!", expectedCancelMessageFound);

	}
	
	/**
	 * After first weave class message, get it to cancel, there should be one more
	 * warning message about cancelling the weave and their should be nothing on the
	 * disk.
	 * 
	 * EvenMoreCode.lst contains:
	 * A1.aj
	 * Cl1.java
	 * A2.aj
	 * Cl2.java
	 * HW.java
	 * A3.aj
	 * Cl3.java
	 * A4.aj
	 * 
	 */
	public void testCancelFirstClassWeave() {
		if (debugTests) System.out.println("testCancelFirstClassWeave: Building with EvenMoreCode.lst");
		compilerAdapter = new CompilerAdapter();
		BuildProgMon programmableBPM = new BuildProgMon();

		programmableBPM.cancelOn("woven class",1); 
	
		compilerAdapter.compile(
			(String) openFile("EvenMoreCode.lst").getAbsolutePath(),
			programmableBPM,
			false);
	
//		Should just be A1 on the disk - uncomment this line to verify that! (and uncomment diskContents())
//		assertTrue("Incorrect disk contents found",diskContents("A1"));

		assertTrue("Should have cancelled after first class weave?:"+programmableBPM.numWovenClassMessages,
		  programmableBPM.numWovenClassMessages==1);
		  
		boolean expectedCancelMessageFound = checkFor("Weaving cancelled as requested");
		if (!expectedCancelMessageFound) dumpTaskData(); // Useful for debugging
		assertTrue("Failed to get warning message about weaving being cancelled!", expectedCancelMessageFound);
	}
	
	
	/**
	 * After first weave aspect message, get it to cancel, there should be one more
	 * warning message about cancelling the weave and their should be nothing on the
	 * disk.
	 * 
	 * EvenMoreCode.lst contains:
	 * A1.aj
	 * Cl1.java
	 * A2.aj
	 * Cl2.java
	 * HW.java
	 * A3.aj
	 * Cl3.java
	 * A4.aj
	 * 
	 */
	public void testCancelSecondClassWeave() {
		if (debugTests) System.out.println("testCancelSecondClassWeave: Building with EvenMoreCode.lst");
		compilerAdapter = new CompilerAdapter();
		BuildProgMon programmableBPM = new BuildProgMon();

		programmableBPM.cancelOn("woven class",2); 
	
		compilerAdapter.compile(
			(String) openFile("EvenMoreCode.lst").getAbsolutePath(),
			programmableBPM,
			false);
	
//		Uncomment this line to verify disk contents(and uncomment diskContents())
//		assertTrue("Incorrect disk contents found",diskContents("A1 Cl1 A2"));

		assertTrue("Should have cancelled after first class weave?:"+programmableBPM.numWovenClassMessages,
		  programmableBPM.numWovenClassMessages==2);
		  
		boolean expectedCancelMessageFound = checkFor("Weaving cancelled as requested");
		if (!expectedCancelMessageFound) dumpTaskData(); // Useful for debugging
		assertTrue("Failed to get warning message about weaving being cancelled!", expectedCancelMessageFound);

	}
	
	
	// ----
	// Helper classes and methods
	

	private class BuildProgMon implements BuildProgressMonitor {
		
        public int numWovenClassMessages = 0;
        public int numWovenAspectMessages = 0;
        public int numCompiledMessages = 0;
        
		private String programmableString;
		private int count;
		private List messagesReceived = new ArrayList();
		private int currentVal;
        
		public void start(String configFile) {
			currentVal = 0;
		}

		public void cancelOn(String string,int count) {
			programmableString = string;
			this.count = count;
		}

		public void setProgressText(String text) {
			String newText = text+" [Percentage="+currentVal+"%]";
			messagesReceived.add(newText);
			if (text.startsWith("woven aspect ")) numWovenAspectMessages++;
			if (text.startsWith("woven class ")) numWovenClassMessages++;
			if (text.startsWith("compiled:")) numCompiledMessages++;
			if (programmableString != null
				&& text.indexOf(programmableString) != -1) {
				count--;
				if (count==0) {
					if (debugTests) System.out.println("Just got message '"+newText+"' - asking build to cancel");
					compilerAdapter.requestCompileExit();
					programmableString = null;
				}
			}
		}
		
		public boolean containsMessage(String prefix,String distinguishingMarks) {
			for (Iterator iter = messagesReceived.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				if (element.startsWith(prefix) &&
				    element.indexOf(distinguishingMarks)!=-1) return true;
			}
			return false;
		}
		
		public void dumpMessages() {
			System.out.println("ProgressMonitorMessages");
			for (Iterator iter = messagesReceived.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				System.out.println(element);
			}
		}

		public void setProgressBarVal(int newVal) {
			this.currentVal = newVal;
		}

		public void incrementProgressBarVal() {
			System.err.println("ipbv");
		}

		public void setProgressBarMax(int maxVal) {
			System.err.println("spbm"+maxVal);
		}

		public int getProgressBarMax() {
			return 100; // Causes setProgressBarVal() to be fed what are effectively percentages
		}

		public void finish(boolean b) {
		}

	}
	
//	private boolean diskContents(String shouldExist) {
//		String[] fullList = new String[] { "A1","A2","A3","A4","Cl1","Cl2","Cl3","HW"};
//		boolean isOK = true;
//		for (int i = 0; i < fullList.length; i++) {
//			String file = fullList[i];
//			if (shouldExist.indexOf(file)!=-1) {
//				// There should be a class file called this
//				if (!openFile("bin/"+file+".class").exists()) {
//					isOK=false; 
//					System.out.println("Couldn't find this expected file: "+file+".class");
//				}
//			} else {
//				// There should NOT be a class file called this
//				if (openFile("bin/"+file+".class").exists()) {
//					isOK=false;
//					System.out.println("Found this file when not expected: "+file+".class");
//				}
//			}
//		}
//		return isOK;
//	}
//	
//	private int wovenClassesFound() {
//		int found = 0;
//		File fA1 = openFile("bin/A1.class");
//		File fA2 = openFile("bin/A2.class");
//		File fA3 = openFile("bin/A3.class");
//		File fA4 = openFile("bin/A4.class");
//		File fHW = openFile("bin/HW.class");
//
//		found+=(fA1.exists()?1:0);
//		found+=(fA2.exists()?1:0);
//		found+=(fA3.exists()?1:0);
//		found+=(fA4.exists()?1:0);
//		found+=(fHW.exists()?1:0);
//		return found;
//	}
	

	private boolean checkFor(String what) {
		List ll = ideManager.getCompilationSourceLineTasks();
		for (Iterator iter = ll.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			if (element.toString().indexOf(what) != -1)
				return true;
		}
		return false;
	}
	
	private void dumpTaskData() {
		if (!debugTests) return;
		List ll = ideManager.getCompilationSourceLineTasks();
		for (Iterator iter = ll.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			System.out.println("RecordedMessage>"+element);
		}
	}

}
