/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.testing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.tools.ajc.AjcTestCase;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AjcTest {
	
	private static boolean is13VMOrGreater = true;
	private static boolean is14VMOrGreater = true;
	private static boolean is15VMOrGreater = false;
	private static boolean is16VMOrGreater = false;
	
	static { // matching logic is also in org.aspectj.util.LangUtil
        String vm = System.getProperty("java.version"); // JLS 20.18.7
        if (vm==null) vm = System.getProperty("java.runtime.version");
		if (vm==null) vm = System.getProperty("java.vm.version");
		if (vm.startsWith("1.3")) {
			is14VMOrGreater = false;
		} else if (vm.startsWith("1.5")) {
			is15VMOrGreater = true;
		} else if (vm.startsWith("1.6")) {
			is15VMOrGreater = true;
			is16VMOrGreater = true;
		}
	}

	private List testSteps = new ArrayList();
	
	private String dir;
	private String pr;
	private String title;
	private String keywords;
	private String comment;
	private String vmLevel = "1.3";

	public AjcTest() {
	}
	
	public void addTestStep(ITestStep step) {
		testSteps.add(step);
		step.setTest(this);
	}
	
	public boolean runTest(AjcTestCase testCase) {
		if (!canRunOnThisVM()) return false;
		try {
			System.out.print("TEST: " + getTitle() + "\t");			
			for (Iterator iter = testSteps.iterator(); iter.hasNext();) {
				ITestStep step = (ITestStep) iter.next();
				step.setBaseDir(getDir());
				System.out.print(".");
				step.execute(testCase);
			}
		} finally {
			System.out.println("DONE");
		}
		return true;
	}
	
	public boolean canRunOnThisVM() {		
		if (vmLevel.equals("1.3")) return true;
		boolean canRun = true;
		if (vmLevel.equals("1.4")) canRun = is14VMOrGreater;
		if (vmLevel.equals("1.5")) canRun = is15VMOrGreater;
		if (vmLevel.equals("1.6")) canRun = is16VMOrGreater;
		if (!canRun) {
			System.out.println("***SKIPPING TEST***" + getTitle()+ " needs " + getVmLevel() 
					+ ", currently running on " + System.getProperty("java.vm.version"));
		}
		return canRun;
	}
	
	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * @return Returns the dir.
	 */
	public String getDir() {
		return dir;
	}
	/**
	 * @param dir The dir to set.
	 */
	public void setDir(String dir) {
		dir = "../tests/" + dir;
		this.dir = dir;
	}
	/**
	 * @return Returns the keywords.
	 */
	public String getKeywords() {
		return keywords;
	}
	/**
	 * @param keywords The keywords to set.
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	/**
	 * @return Returns the pr.
	 */
	public String getPr() {
		return pr;
	}
	/**
	 * @param pr The pr to set.
	 */
	public void setPr(String pr) {
		this.pr = pr;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param vmLevel The vmLevel to set.
	 */
	public void setVm(String vmLevel) {
		this.vmLevel = vmLevel;
	}
	
	/**
	 * @return Returns the vmLevel.
	 */
	public String getVmLevel() {
		return vmLevel;
	}
}
