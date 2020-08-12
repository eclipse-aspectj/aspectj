/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import junit.framework.TestCase;
import org.aspectj.ajdt.ajc.BuildArgParser;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AjStateTest extends TestCase {

	private AjState aRightState;
	private AjBuildConfig oldConfig;
	private AjBuildConfig newConfig;

	public void testNoChange() {
		aRightState.setCouldBeSubsequentIncrementalBuild(true);
		assertTrue("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testAddEntryToClasspath() {
		newConfig.getClasspath().add("anotherEntry");
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testRemoveEntryFromClasspath() {
		newConfig.getClasspath().remove(0);
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testReorderClasspath() {
		String o = newConfig.getClasspath().remove(0);
		newConfig.getClasspath().add(o);
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testAddEntryToAspectpath() {
		newConfig.addToAspectpath(new File("anotherEntry.jar"));
//		newConfig.getAspectpath().add(new File("anotherEntry.jar"));
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testRemoveEntryFromAspectpath() {
		newConfig.removeAspectPathEntry(0);
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testReorderAspectpath() {
		String o = newConfig.removeClasspathEntry(0);
		newConfig.addToAspectpath(new File(o));
//		newConfig.getAspectpath().add(new File(o));
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testAddEntryToInpath() {
		newConfig.addToInpath(new File("anotherEntry"));
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testRemoveEntryFromInpath() {
		newConfig.removeInpathEntry(0);//getInpath().remove(0);
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testReorderInpath() {
		String o = newConfig.removeClasspathEntry(0);//getClasspath().remove(0);
		newConfig.addToInpath(new File(o));//getInpath().add(new File(o));
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testAddEntryToInjars() {
		newConfig.addToInjars(new File("anotherEntry.jar"));
//		newConfig.getInJars().add(new File("anotherEntry.jar"));
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testRemoveEntryFromInjars() {
		newConfig.removeInjarsEntry(0);
//		newConfig.getInJars().remove(0);
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	public void testReorderInjars() {
		String o = newConfig.getClasspath().remove(0);
		newConfig.getInJars().add(new File(o));
		assertFalse("Can do incremental", aRightState.prepareForNextBuild(newConfig));
	}

	protected void setUp() throws Exception {
		super.setUp();
		aRightState = new AjState(null);
		final BuildArgParser parser = new BuildArgParser(new IMessageHandler() {
			public boolean handleMessage(IMessage message) throws AbortException {
				return true;
			}

			public boolean isIgnoring(IMessage.Kind kind) {
				return false;
			}

			public void dontIgnore(IMessage.Kind kind) {
			}

			public void ignore(IMessage.Kind kind) {
			}
		});
		oldConfig = new AjBuildConfig(parser);
		newConfig = new AjBuildConfig(parser);
		List<String> cp = new ArrayList<>();
		cp.add("adir");
		cp.add("ajar.jar");
		oldConfig.setClasspath(cp);
		newConfig.setClasspath(new ArrayList<>(cp));
		List<File> ap = new ArrayList<>();
		ap.add(new File("aLib.jar"));
		ap.add(new File("anotherLib.jar"));
		oldConfig.setAspectpath(ap);
		newConfig.setAspectpath(new ArrayList<>(ap));
		List<File> ip = new ArrayList<>();
		ip.add(new File("adir"));
		ip.add(new File("ajar.jar"));
		oldConfig.setInPath(ip);
		newConfig.setInPath(new ArrayList<>(ip));
		List<File> ij = new ArrayList<>();
		ij.add(new File("aLib.jar"));
		ij.add(new File("anotherLib.jar"));
		oldConfig.setInJars(ij);
		newConfig.setInJars(new ArrayList<>(ij));
		aRightState.prepareForNextBuild(oldConfig);
		aRightState.successfulCompile(oldConfig, true);
	}
}
