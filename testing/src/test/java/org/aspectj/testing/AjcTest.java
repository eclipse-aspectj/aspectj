/* *******************************************************************
 * Copyright (c) 2004,2019 IBM Corporation, contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * ******************************************************************/
package org.aspectj.testing;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.util.LangUtil;

/**
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class AjcTest {

	//	private static boolean is13VMOrGreater = true;
	private static boolean is1dot4VMOrGreater = true;
	private static boolean is15VMOrGreater = false;
	private static boolean is16VMOrGreater = false;
	private static boolean is17VMOrGreater = false;
	private static boolean is18VMOrGreater = false;
	private static boolean is19VMOrGreater = false;
	private static boolean is10VMOrGreater = false;
	private static boolean is11VMOrGreater = false;
	private static boolean is12VMOrGreater = false;
	private static boolean is13VMOrGreater = false;
	private static boolean is14VMOrGreater = false;

	static { // matching logic is also in org.aspectj.util.LangUtil
		is1dot4VMOrGreater = true;
		is15VMOrGreater = true;
		is16VMOrGreater = true;
		is17VMOrGreater = true;
		is18VMOrGreater = true;
		is19VMOrGreater = LangUtil.is19VMOrGreater();
		is10VMOrGreater = LangUtil.is10VMOrGreater();
		is11VMOrGreater = LangUtil.is11VMOrGreater();
		is12VMOrGreater = LangUtil.is12VMOrGreater();
		is13VMOrGreater = LangUtil.is13VMOrGreater();
		is14VMOrGreater = LangUtil.is14VMOrGreater();
	}

	private List<ITestStep> testSteps = new ArrayList<>();

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
			for (ITestStep step: testSteps) {
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
		if (vmLevel.equals("1.4")) canRun = is1dot4VMOrGreater;
		if (vmLevel.equals("1.5")) canRun = is15VMOrGreater;
		if (vmLevel.equals("1.6")) canRun = is16VMOrGreater;
		if (vmLevel.equals("1.7")) canRun = is17VMOrGreater;
		if (vmLevel.equals("1.8")) canRun = is18VMOrGreater;
		if (vmLevel.equals("1.9")) canRun = is19VMOrGreater;
		if (vmLevel.equals("10")) canRun = is10VMOrGreater;
		if (vmLevel.equals("11")) canRun = is11VMOrGreater;
		if (vmLevel.equals("12")) canRun = is12VMOrGreater;
		if (vmLevel.equals("13")) canRun = is13VMOrGreater;
		if (vmLevel.equals("14")) canRun = is14VMOrGreater;
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
