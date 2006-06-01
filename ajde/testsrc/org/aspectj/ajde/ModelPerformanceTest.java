/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.ajde;

/**
 * @author Mik Kersten
 */
public class ModelPerformanceTest extends AjdeTestCase {

    // TODO-path
	private final String CONFIG_FILE_PATH = "../examples/spacewar/spacewar/debug.lst";
  
	public ModelPerformanceTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(ModelPerformanceTest.class);
	}
	public void testNothingButKeepAntJUnitHappy() {}
	/**
	 * Assert that a compile that includes building the structure model
	 * adds an acceptable percentage of overhead.
	 * 
	 * Does a few initial builds to minimize caching effects.
	 */
	public void skip_testRelativeToNoModel() {
		timedBuild(true);
		timedBuild(false);
		timedBuild(true);
		
		long rawBuildTime = timedBuild(false);
		long modelBuildTime = timedBuild(true);
		float overhead = (float)modelBuildTime / (float)rawBuildTime;
		
		assertTrue("overhead is " + overhead + " > 1.3", overhead < 1.3);
//		System.err.println("> overhead: " + overhead);
	}

	public long timedBuild(boolean buildModel) {
		long startTime = System.currentTimeMillis();
		doSynchronousBuild(CONFIG_FILE_PATH);
		Ajde.getDefault().getBuildManager().setBuildModelMode(buildModel);
		long endTime = System.currentTimeMillis();
		return  (endTime - startTime);
		
	}

	protected void setUp() throws Exception {
		super.setUp("examples");	
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}

