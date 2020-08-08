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

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.aspectj.ajdt.ajc.Constants;
import org.aspectj.testing.util.TestUtil;

public class WorkingTestMain {

	public static void main(String[] args1) throws IOException {
		//testExamples();
		testOne();
	}
	
	public static void testOne() throws IOException {
		//CommandTestCase.checkCompile("src1/Parents.java", CommandTestCase.NO_ERRORS);
		
		//CommandTestCase.checkCompile("../../tests/new/ArgsInCflow2.java", CommandTestCase.NO_ERRORS);
		
		//CommandTestCase.checkCompile("src1/ParentsFail.java", CommandTestCase.NO_ERRORS);
		
		List args = new ArrayList();
		args.add("-verbose");
		
		//args.add("-1.3");
		args.add("-d");
		args.add("out");
		
		args.add("-classpath");
        args.add(Constants.aspectjrtClasspath() + File.pathSeparator
                + "../testing-client/bin");
		//args.add("../runtime/bin;../lib/junit/junit.jar");
		
//		args.add("-injars");
//		args.add("testdata/testclasses.jar");
		
		//args.add("-aspectpath");
		//args.add("../weaver/testdata/megatrace.jar");
		
		args.add("c:/aspectj/scratch/arno/*.java");
		//args.add("-XnoInline");
		//args.add("../tests/new/Counting1.java");
		//args.add("-Xlint:error");
		//args.add("testdata/src1/InterType.java");
		//args.add("@" + examplesDir + "tjp/files.lst");
		
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		//CommandTestCase.runCompiler(args, new int[] {11, 14, 18, 32, 43});
		
		//CommandTestCase.printGenerated("../out", "AroundA1");
//		CommandTestCase.printGenerated("../out", "SuperC");
		CommandTestCase.printGenerated("../out", "org.schmidmeier.unittests.cache.TimeCacheTestsWorking");

		TestUtil.runMain("out;../lib/test/testing-client.jar", "org.schmidmeier.unittests.cache.AllTimeCacheTests");
	}
	
	private static String examplesDir = "../docs/dist/doc/examples/";
	private static void example(String[] argfiles, String[] classes) {
		List args = new ArrayList();
		args.add("-verbose");
		
		args.add("-d");
		args.add("out");
		
		args.add("-classpath");
		args.add(Constants.aspectjrtClasspath());

		for (String argfile : argfiles) {
			args.add("@" + examplesDir + argfile);
		}
		
		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
		for (String aClass : classes) {
			TestUtil.runMain("out", aClass);
		}		
	}
	
	
	public static void testExamples() throws IOException {
		example(new String[] {"observer/files.lst"}, 
				new String[] {}); // don't run the gui
				
		example(new String[] {"tjp/files.lst"}, 
				new String[] {"tjp.Demo"});
				
		example(new String[] {"telecom/timing.lst"}, 
				new String[] {"telecom.TimingSimulation"});
				
		example(new String[] {"telecom/billing.lst"}, 
				new String[] {"telecom.BillingSimulation"});
				
		example(new String[] {"tracing/tracev1.lst"}, 
				new String[] {"tracing.version1.TraceMyClasses"});
				
		example(new String[] {"tracing/tracev2.lst"}, 
				new String[] {"tracing.version2.TraceMyClasses"});
				
		example(new String[] {"tracing/tracev3.lst"}, 
				new String[] {"tracing.version3.TraceMyClasses"});
				

		example(new String[] {"introduction/files.lst"}, 
				new String[] {"introduction.HashablePoint", "introduction.ComparablePoint"});


				
		
		example(new String[] {"bean/files.lst"}, 
				new String[] {"bean.Demo"});
				
		example(new String[] {"spacewar/demo.lst"}, 
				new String[] {});  // don't run the gui
				
		example(new String[] {"spacewar/debug.lst"}, 
				new String[] {});  // don't run the gui
				
		System.out.println("done!!!!!!!!!!!!!!!!!!!!");
	}
		
}
