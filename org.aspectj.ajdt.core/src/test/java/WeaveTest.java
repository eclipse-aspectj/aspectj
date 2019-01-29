/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Noel Markham, Matthew Webster     initial implementation 
 * ******************************************************************/

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * @author Noel Markham
 */
public class WeaveTest {
	
	private static final String OUTPUT_PACKAGE = "out";
	
	private static final int WEAVE_FAST = 1;
	private static final int WEAVE_MED = 2;
	private static final int WEAVE_SLOW = 3;
	
	public static final String EXECUTION_SLOW = "execution(void *(..))";
	public static final String EXECUTION_MED = "args(" + OUTPUT_PACKAGE + ".C0) && execution(void m0(..))";
	public static final String EXECUTION_FAST = "within(" + OUTPUT_PACKAGE + ".C0) && execution(void m0(..))";
	
	public static final String GET_SLOW = "get(int *)";
	public static final String GET_MED = "target(" + OUTPUT_PACKAGE + ".C0) && get(int i0)";
	public static final String GET_FAST = "get(int " + OUTPUT_PACKAGE + ".C0.i0)";
	
	// Defaults, can be changed with command-line args
	private static int NUMCLASSES = 5;
	private static int ITERATIONS = 3;
	private static int METHODLINES = 20;
	private static int NUMMETHODS = 100;
	private static boolean INCLUDE_TEST_CLASSES = false;
	private static boolean TEST_GET = true;
	private static boolean TEST_EXECUTION = true;

	private static boolean ALL_POINTCUT_TESTS = true;
	private static boolean TEST_ONE = false;
	private static boolean TEST_ONE_SEARCH_ALL = false;
	private static boolean TEST_ALL = false;
	private static boolean ECHO = false;

	public static long[] compileTimes = new long[ITERATIONS];
	public static long[] executionSlowTimes = new long[ITERATIONS];
	public static long[] executionMedTimes = new long[ITERATIONS];
	public static long[] executionFastTimes = new long[ITERATIONS];	
	public static long[] getSlowTimes = new long[ITERATIONS];
	public static long[] getMedTimes = new long[ITERATIONS];
	public static long[] getFastTimes = new long[ITERATIONS];

	private static final String NL = System.getProperty("line.separator", "\n");
	
	private static final File outputDir = new File(System.getProperty("user.dir") + File.separatorChar + OUTPUT_PACKAGE);

	static {
		outputDir.mkdirs();
	}

	public static void main(String[] args) throws IOException {

		//if (args.length > 0) parseArgs(args);
		parseArgs(args);
		
		if(ECHO) System.out.println("Weave-time Test");
		if(ECHO) System.out.println("---------------");
		
		if(ECHO) System.out.println("Number of classes: " + NUMCLASSES);
		if(ECHO) System.out.println("Number of methods: " + NUMMETHODS);
		if(ECHO) System.out.println("Number of method lines: " + METHODLINES);
		if(ECHO) System.out.println("Number of test iterations: " + ITERATIONS);
		if(ECHO) 
			if(INCLUDE_TEST_CLASSES) System.out.println("Including advice test classes");
		if(ECHO)
			if(TEST_GET && !TEST_EXECUTION) System.out.println("Weaving only get advice");
		if(ECHO)
			if(TEST_EXECUTION && !TEST_GET) System.out.println("Weaving only execution advice");
		if(ECHO) {
			if(!ALL_POINTCUT_TESTS) {
				if(TEST_ONE) System.out.println("Weaving one poinctcut");
				if(TEST_ONE_SEARCH_ALL) System.out.println("Weaving one pointcut, searching all");
				if(TEST_ALL) System.out.println("Weaving all");
			}
		}

		if(ECHO) System.out.println();
		
		createClasses();
		compileClasses();

		boolean warm = false;		

		if (TEST_EXECUTION) {
			String advice = "execution";
			createAspects(advice);
			
			//Warm up the weaver
			weaveAllAspects(advice, WEAVE_FAST);
			warm = true;
			
			weaveAspects(advice);
		}
		
		if (TEST_GET) {
			String advice = "get";
			createAspects(advice);
			
			if(!warm) weaveAllAspects(advice, WEAVE_FAST);
			weaveAspects(advice);
		}
	}
	
	private static void parseArgs(String[] args) {
		
		if(args == null || args.length <= 0) return;

		int i = 0;
		boolean error = false;
		
		while(i < args.length) {
			String arg = args[i++];
			
			try {
				if(arg.equals("-c")) { // Number of classes
					if(i < args.length)
						NUMCLASSES = Integer.parseInt(args[i++]);
					else error = true;
				}
				else if(arg.equals("-m")) { // Number of methods
					if(i < args.length)
						NUMMETHODS = Integer.parseInt(args[i++]);
					else error = true;
				}
				else if(arg.equals("-l")) { // Number of method lines
					if(i < args.length)
						METHODLINES = Integer.parseInt(args[i++]);
					else error = true;
				}
				else if(arg.equals("-i")) { // Number of iterations
					if(i < args.length)
						ITERATIONS = Integer.parseInt(args[i++]);
					else error = true;
				}
				else if(arg.equals("-include-tests")) {
					if(i < args.length) {
						arg = args[i++];
						if(arg.equalsIgnoreCase("y")) INCLUDE_TEST_CLASSES = true;
						else if(arg.equalsIgnoreCase("n")) INCLUDE_TEST_CLASSES = false;
						else error = true;
					}
				}
				else if(arg.equals("-advice")) {
					String advice = args[i++];
					if(advice.equals("get")){
						TEST_GET = true;
						TEST_EXECUTION = false;
					}
					else if(advice.equals("execution")){
						TEST_EXECUTION = true;
						TEST_GET = false;
					}
					else error = true;
				}
				else if(arg.equals("-pointcut")) {
					ALL_POINTCUT_TESTS = false;
					String advice = args[i++];
					if(advice.equals("fast")) TEST_ONE = true;
					else if(advice.equals("meduim")) TEST_ONE_SEARCH_ALL = true;
					else if(advice.equals("slow")) TEST_ALL = true;
					else error = true;
				}
				else if(arg.equals("-echo")) {
					ECHO = true;
				}
				else if (arg.equals("-help")) {
					usage();
				}
				else error = true;
			} catch (NumberFormatException e) {
				usage();
			}
			
			if(error) usage();
			
			compileTimes = new long[ITERATIONS];
			executionSlowTimes = new long[ITERATIONS];
			executionMedTimes = new long[ITERATIONS];
			executionFastTimes = new long[ITERATIONS];	
			getSlowTimes = new long[ITERATIONS];
			getMedTimes = new long[ITERATIONS];
			getFastTimes = new long[ITERATIONS];

		}
	}

	private static void usage() {
		System.err.println("Usage:");
		System.err.println("\tjava WeaveTests [-c num_of_classes] [-m num_of_methods] " +
			"[-l num_of_method_lines] [-i num_of_iterations]" +
			"\n\t\t[-include-tests y|n] [-advice get|execution] [-pointcut fast|medium|slow] [-echo] [-help]");
			
		System.exit(-1);
	}
	
	/**
	 * Will create a number of classes of the following form:
	 * 
	 * public class C0 {
	 * 
	 * 		int i0;
	 * 		...
	 * 		int iN;
	 * 
	 * 		void m0(C0 arg) {
	 * 			i0++;
	 * 			...
	 * 			iN++;
	 * 		}
	 * 		...
	 * 		void mN(C0 arg) {
	 * 		...
	 * }
	 * 
	 */
	public static void createClasses() throws IOException {
		
		if(ECHO) System.out.println("Creating classes");
		
		for(int classcounter = 0; classcounter < NUMCLASSES; classcounter++) {
			
			StringBuffer classfile = new StringBuffer(1000);
			
			classfile.append("// Auto-generated" + NL);
			classfile.append("package " + OUTPUT_PACKAGE + ";" + NL + NL);
			
			classfile.append("public class C" + classcounter + " {" + NL + NL);
			
			for(int intdeclaration = 0; intdeclaration < METHODLINES; intdeclaration++) {
				classfile.append("\tint i" + intdeclaration + ";" + NL);
			}
			classfile.append("\tint getter;" + NL);
			
			classfile.append(NL);
			
			for(int methodcounter = 0; methodcounter < NUMMETHODS; methodcounter++) {
				classfile.append("\tvoid m" + methodcounter + "(C" + classcounter + " arg) {" + NL);
				
				for(int methodbody = 0; methodbody < METHODLINES; methodbody++) {
					classfile.append("\t\ti" + methodbody + "++;" + NL);
				}				
				
				classfile.append("\t}" + NL + NL);	
			}

			classfile.append("}" + NL);
						
			try {
				
				File f = new File(outputDir, ("C" + classcounter + ".java"));
				FileOutputStream fos = new FileOutputStream(f);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				
				bos.write(new String(classfile).getBytes());
				
				bos.close();
				fos.close();
								
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		if (INCLUDE_TEST_CLASSES) {
			StringBuffer testFiles = new StringBuffer(1000);

			try {
				testFiles.append("// Auto generated" + NL);
				testFiles.append("package " + OUTPUT_PACKAGE + ";" + NL + NL);

				testFiles.append("public class TestGet {" + NL + NL);
				testFiles.append(
					"\tpublic static void main(String args[]) {" + NL);
				testFiles.append("\t\tC0 tester = new C0();" + NL);
				testFiles.append("\t\tint i = tester.i0;" + NL);
				testFiles.append("\t}" + NL);
				testFiles.append("}" + NL);

				File f = new File(outputDir, "TestGet.java");
				FileOutputStream fos = new FileOutputStream(f);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				bos.write(new String(testFiles).getBytes());
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}

			testFiles = new StringBuffer(1000);

			try {
				testFiles.append("// Auto generated" + NL);
				testFiles.append("package " + OUTPUT_PACKAGE + ";" + NL + NL);

				testFiles.append("public class TestExecution {" + NL + NL);
				testFiles.append(
					"\tpublic static void main(String args[]) {" + NL);
				testFiles.append("\t\tC0 tester = new C0();" + NL);
				testFiles.append("\t\ttester.m0(tester);" + NL);
				testFiles.append("\t}" + NL);
				testFiles.append("}" + NL);

				File f = new File(outputDir, "TestExecution.java");
				FileOutputStream fos = new FileOutputStream(f);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				bos.write(new String(testFiles).getBytes());
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		StringBuffer buildList = new StringBuffer(100);
		
		for(int i = 0; i < NUMCLASSES; i++)
			buildList.append("C" + i + ".java" + NL);
			
		if (INCLUDE_TEST_CLASSES) {
			buildList.append("TestGet.java" + NL);
			buildList.append("TestExecution.java" + NL);
		}
			
		try {
			File f = new File(outputDir, "build.lst");
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			bos.write(new String(buildList).getBytes());
			
			bos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * @param adviceType Either "get" or "execution" depending on which test.
	 * 
	 * Will create an aspect such as the following:
	 * 
	 * public aspect GetAdviceWeaveOne {
	 * 
	 * 	before() : get(int output.C0.getter) {
	 * 		System.out.println("In the aspect");
	 * 	}
	 * }
	 */
	public static void createAspects(String adviceType) {
		
		adviceType = adviceType.toLowerCase();
		
		if((!adviceType.equals("get")) && (!adviceType.equals("execution"))) {
			System.err.println("Only get and execution advice is supported");
			System.exit(-1);
		}
		
		if(ECHO) System.out.println("Creating aspects");
		
		if(ALL_POINTCUT_TESTS || TEST_ONE)
			createAllAspects(adviceType, WEAVE_FAST);
		if(ALL_POINTCUT_TESTS || TEST_ONE_SEARCH_ALL)
			createAllAspects(adviceType, WEAVE_MED);
		if(ALL_POINTCUT_TESTS || TEST_ALL)
			createAllAspects(adviceType, WEAVE_SLOW);
	}
	
	private static void createAllAspects(String adviceType, int pointcut) {
	
		StringBuffer aspectFile = new StringBuffer(1000);

		// Capitalises the first char in the adviceType String, and then adds "Advice" to it.
		String adviceName = (char)(adviceType.charAt(0) - 32) + adviceType.substring(1) + "Advice";
		
		switch(pointcut) {
			case WEAVE_FAST:
					adviceName += "WeaveFast";
					break;
			case WEAVE_MED:
					adviceName += "WeaveMedium";
					break;
			case WEAVE_SLOW:
					adviceName += "WeaveSlow";
					break;
		}
		
		aspectFile.append("// Auto-generated" + NL + NL);
		
		aspectFile.append("public aspect " + adviceName + " {" + NL + NL);
		aspectFile.append("\tbefore() : ");
		
		if(adviceType.equals("execution")) {
			switch(pointcut) {
				case WEAVE_FAST:
					aspectFile.append(EXECUTION_FAST);
					break;
				case WEAVE_MED:
					aspectFile.append(EXECUTION_MED);
					break;
				case WEAVE_SLOW:
					aspectFile.append(EXECUTION_SLOW);
					break;
			}
		}
		else {
			switch(pointcut) {
				case WEAVE_FAST:
					aspectFile.append(GET_FAST);
					break;
				case WEAVE_MED:
					aspectFile.append(GET_MED);
					break;
				case WEAVE_SLOW:
					aspectFile.append(GET_SLOW);
					break;
			}
		}
		
		aspectFile.append(" {" + NL);
		
		aspectFile.append("\t\tSystem.out.println(\"In the aspect\");" + NL);
		aspectFile.append("\t}" + NL);
		aspectFile.append("}" + NL);

		// Create the file
		try {
			File f = new File(outputDir, (adviceName + ".aj"));
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			bos.write(new String(aspectFile).getBytes());
			
			bos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void compileClasses() throws IOException {
		
		if(ECHO) System.out.print("Compiling: ");

		long average = 0;
		
		for (int i = 0; i < ITERATIONS; i++) {
			long time = performCompile();
			compileTimes[i] = time;
			average += time;
		}
		
		if(ECHO) System.out.println((average / ITERATIONS) + " millis");
	}
	
	private static long performCompile() throws IOException {
		
		String ajcargs = 
		  "-noExit -outjar " + OUTPUT_PACKAGE + File.separatorChar + "classes.jar " + 
		  "-argfile " + OUTPUT_PACKAGE + File.separatorChar + "build.lst";

		// split method creates a String array delimited on a space
		String[] parsedArgs = RunWeaveTests.split(ajcargs); 

		
		long start = System.currentTimeMillis();
		
		org.aspectj.tools.ajc.Main.main(parsedArgs);
		
		long stop = System.currentTimeMillis();
		
		return stop - start;
	}
	
	
	public static void weaveAspects(String adviceType) throws IOException {

		adviceType = adviceType.toLowerCase();
		
		if((!adviceType.equals("get")) && (!adviceType.equals("execution"))) {
			System.err.println("Only get and execution advice is supported");
			System.exit(-1);
		}
		
		long average = 0;
		
		if(ECHO) System.out.println((char)(adviceType.charAt(0) - 32) + adviceType.substring(1));
		
		if (ALL_POINTCUT_TESTS || TEST_ONE) {
			if(ECHO) System.out.print("Weave Fast:");
			for (int i = 0; i < ITERATIONS; i++) {
				long time = weaveAllAspects(adviceType, WEAVE_FAST);
				
				if(adviceType.equals("execution")) executionFastTimes[i] = time;
				else getFastTimes[i] = time;
				
				average += time;
				if(ECHO) System.out.print(".");
			}
			if(ECHO) System.out.println(" " + (average / ITERATIONS) + " millis");
		}
		
		
		average = 0;
		
		if (ALL_POINTCUT_TESTS || TEST_ONE_SEARCH_ALL) {
			if(ECHO) System.out.print("Weave Medium:");
			for (int i = 0; i < ITERATIONS; i++) {
				long time = weaveAllAspects(adviceType, WEAVE_MED);
				
				if(adviceType.equals("execution")) executionMedTimes[i] = time;
				else getMedTimes[i] = time;
				
				average += time;
				if(ECHO) System.out.print(".");
			}
			if(ECHO) System.out.println(" " + (average / ITERATIONS) + " millis");
		}
		
		
		average = 0;
		
		if (ALL_POINTCUT_TESTS || TEST_ALL) {
			if(ECHO) System.out.print("Weave Slow:");
			for (int i = 0; i < ITERATIONS; i++) {
				long time = weaveAllAspects(adviceType, WEAVE_SLOW);

				if(adviceType.equals("execution")) executionSlowTimes[i] = time;
				else getSlowTimes[i] = time;
				
				average += time;
				if(ECHO) System.out.print(".");
			}
			if(ECHO) System.out.println(" " + (average / ITERATIONS) + " millis");
		}

		if(ECHO) System.out.println();
	}
	
	private static long weaveAllAspects(String adviceType, int pointcut) throws IOException {

		// Capitalises the first char in the adviceType String, to keep to Java naming convention
		String adviceName = (char)(adviceType.charAt(0) - 32) + adviceType.substring(1) + "Advice";
		
		switch(pointcut) {
			case WEAVE_FAST:
					adviceName += "WeaveFast";
					break;
			case WEAVE_MED:
					adviceName += "WeaveMedium";
					break;
			case WEAVE_SLOW:
					adviceName += "WeaveSlow";
					break;
		}
		
		String ajcargs =
			"-noExit -injars " + OUTPUT_PACKAGE + File.separatorChar + "classes.jar " + 
			"-outjar " + OUTPUT_PACKAGE + File.separatorChar + adviceName + ".jar " + 
			OUTPUT_PACKAGE + File.separatorChar + adviceName + ".aj";
			
		String[] parsedArgs = RunWeaveTests.split(ajcargs);
		
		long start = System.currentTimeMillis();
		
		org.aspectj.tools.ajc.Main.main(parsedArgs);
		
		long stop = System.currentTimeMillis();
		
		return stop - start;
	}
}