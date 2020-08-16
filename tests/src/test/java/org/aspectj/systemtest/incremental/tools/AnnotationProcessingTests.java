/*******************************************************************************
 * Copyright (c) 2014 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.aspectj.util.FileUtil;

public class AnnotationProcessingTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {
	
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		new File("Advise_aaa.java").delete();
		new File("Advise_ccc.java").delete();
		new File("Advise_boo.java").delete();
		new File("Advise_too.java").delete();
		new File("AroundAdvise_aaa.java").delete();
		new File("AroundAdvise_ccc.java").delete();
		if (new File("../run-all-junit-tests/generated/test/SomeCallbacks.java").exists()) {
			FileUtil.deleteContents(new File("../run-all-junit-tests/generated"));
			new File("../run-all-junit-tests/generated").delete();
		}
	}
	
	// Basic test: turns on annotation processing and tries to run the DemoProcessor
	public void testAnnotationProcessing1() throws Exception {
		createAndBuildAnnotationProcessorProject("ProcessorProject");
		initialiseProject("ProcessorConsumer1");
		configureProcessorOptions("ProcessorConsumer1","DemoProcessor");
		configureNonStandardCompileOptions("ProcessorConsumer1", "-showWeaveInfo");

		Map<String, String> javaOptions = new Hashtable<>();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.source", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.processAnnotations","enabled");
		configureJavaOptionsMap("ProcessorConsumer1", javaOptions);
		
		configureNewProjectDependency("ProcessorConsumer1", "ProcessorProject");
		configureNonStandardCompileOptions("ProcessorConsumer1", "-showWeaveInfo");
		build("ProcessorConsumer1");
		checkWasFullBuild();
		checkCompiledFiles("ProcessorConsumer1","Advise_ccc.java","Advise_aaa.java","Code.java");
		assertEquals(2,getWeavingMessages("ProcessorConsumer1").size());
		String out = runMethod("ProcessorConsumer1", "Code", "runner");
		assertEquals("aaa running\nccc running\n",out.replace("\r",""));
	}
	
	// services file in processor project
	public void testAnnotationProcessing2() throws Exception {
		createAndBuildAnnotationProcessorProject("ProcessorProject2"); // This has a META-INF services entry for DemoProcessor
		
		initialiseProject("ProcessorConsumer2"); 
		// Paths here are the path to DemoProcessor (compiled into the output folder of the ProcessorProject2) and the path to
		// the META-INF file declaring DemoProcessor (since it is not copied to that same output folder) - this exists in the test src
		// folder for ProcessorProject2
		configureProcessorPath("ProcessorConsumer2", getCompilerForProjectWithName("ProcessorProject2").getCompilerConfiguration().getOutputLocationManager().getDefaultOutputLocation().toString()+File.pathSeparator+
				new File(testdataSrcDir + File.separatorChar + "ProcessorProject2" + File.separatorChar + "base"+File.separatorChar+"src").toString());
		
		Map<String, String> javaOptions = new Hashtable<>();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.source", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.processAnnotations","enabled");
		configureJavaOptionsMap("ProcessorConsumer2", javaOptions);
		initialiseProject("ProcessorConsumer2");
		configureNewProjectDependency("ProcessorConsumer2", "ProcessorProject");
		configureNonStandardCompileOptions("ProcessorConsumer2", "-showWeaveInfo");
		build("ProcessorConsumer2");
		checkWasFullBuild();	
		checkCompiledFiles("ProcessorConsumer2","Advise_ccc.java","Advise_aaa.java","Code.java");
		assertEquals(2,getWeavingMessages("ProcessorConsumer2").size());
		String out = runMethod("ProcessorConsumer2", "Code", "runner");
		assertEquals("aaa running\nccc running\n",out.replace("\r",""));
	}
	
	// Two processors
	public void testAnnotationProcessing3() throws Exception {
		createAndBuildAnnotationProcessorProject("ProcessorProject2");
		createAndBuildAnnotationProcessorProject("ProcessorProject3");
		initialiseProject("ProcessorConsumer1");
		// Paths here are the path to DemoProcessor/DemoProcessor2 compiled code and the path to
		// the META-INF file declaring DemoProcessor/DemoProcessor2 (since they are not copied to that same output folder) - 
		// these exists in the test src folders for ProcessorProject2/ProcessorProject3
		configureProcessorPath("ProcessorConsumer1", 
				getCompilerForProjectWithName("ProcessorProject3").getCompilerConfiguration().getOutputLocationManager().getDefaultOutputLocation().toString()+File.pathSeparator+
				new File(testdataSrcDir + File.separatorChar + "ProcessorProject3" + File.separatorChar + "base"+File.separatorChar+"src").toString()
				+File.pathSeparator+
				getCompilerForProjectWithName("ProcessorProject2").getCompilerConfiguration().getOutputLocationManager().getDefaultOutputLocation().toString()+File.pathSeparator+
				new File(testdataSrcDir + File.separatorChar + "ProcessorProject2" + File.separatorChar + "base"+File.separatorChar+"src").toString()
				);
		
		// The order here is DemoProcessor2 then DemoProcessor - to get the second one to run I changed DemoProcessor2 to operate on a
		// specific annotation (java.lang.SuppressWarnings) and return false at the end

		configureNonStandardCompileOptions("ProcessorConsumer1", "-showWeaveInfo");

		Map<String, String> javaOptions = new Hashtable<>();
		javaOptions.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.source", "1.6");
		javaOptions.put("org.eclipse.jdt.core.compiler.processAnnotations","enabled");
		configureJavaOptionsMap("ProcessorConsumer1", javaOptions);
		
		configureNewProjectDependency("ProcessorConsumer1", "ProcessorProject");
		configureNonStandardCompileOptions("ProcessorConsumer1", "-showWeaveInfo");
		build("ProcessorConsumer1");
		checkWasFullBuild();
		checkCompiledFiles("ProcessorConsumer1","Advise_ccc.java","Advise_aaa.java","Code.java","AroundAdvise_ccc.java","AroundAdvise_aaa.java");
		assertEquals(4,getWeavingMessages("ProcessorConsumer1").size());
		String out = runMethod("ProcessorConsumer1", "Code", "runner");
		assertEquals("aaa running\nAround advice on aaa running\nccc running\nAround advice on ccc running\n",out.replace("\r",""));
	}
	
	// Tests:
	// TODO Incremental compilation - what does that mean with annotation processors?

	// ---

	private void createAndBuildAnnotationProcessorProject(String processorProjectName) {
		initialiseProject(processorProjectName);
		build(processorProjectName);
		checkWasFullBuild();
		assertNoErrors(processorProjectName);
	}

	private void configureProcessorOptions(String projectName, String processor) {
		configureProcessor(projectName, "DemoProcessor");
		// Assume all processors from processor project
		configureProcessorPath(projectName, getCompilerForProjectWithName("ProcessorProject").getCompilerConfiguration().getOutputLocationManager().getDefaultOutputLocation().toString());
	}
	
	private void checkCompiledFiles(String projectName, String... expectedCompiledFiles) {
		List<String> compiledFiles = new ArrayList<>(getCompiledFiles(projectName));
		if (compiledFiles.size()!=expectedCompiledFiles.length) {
			fail("Expected #"+expectedCompiledFiles.length+" files to be compiled but found that #"+compiledFiles.size()+" files were compiled.\nCompiled="+compiledFiles);
		}
		for (String expectedCompiledFile: expectedCompiledFiles) {
			String toRemove = null;
			for (String compiledFile: compiledFiles) {
				String cfile = compiledFile.substring(compiledFile.lastIndexOf(File.separator)+1);
				if (cfile.equals(expectedCompiledFile)) {
					toRemove = compiledFile;
					break;
				}
			}
			if (toRemove!=null) compiledFiles.remove(toRemove);
		}
		// Anything left in compiledFiles wasn't expected to be built
		if (compiledFiles.size()!=0) {
			fail("These were not expected to be compiled: "+compiledFiles);
		}
	}
	
	
}
