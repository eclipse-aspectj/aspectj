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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;


public class AjStateTest extends TestCase {

    private AjState aRightState;
    private AjBuildConfig oldConfig;
    private AjBuildConfig newConfig;
    
    public void testNoChange() {
    	aRightState.setCouldBeSubsequentIncrementalBuild(true);
        assertTrue("Can do incremental",aRightState.prepareForNextBuild(newConfig));
    }
    
    public void testAddEntryToClasspath() {
        newConfig.getClasspath().add("anotherEntry");
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));
    }
    
    public void testRemoveEntryFromClasspath() {
        newConfig.getClasspath().remove(0);
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));               
    }
    
    public void testReorderClasspath() {
        Object o = newConfig.getClasspath().remove(0);
        newConfig.getClasspath().add(o);
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));                       
    }

    public void testAddEntryToAspectpath() {
        newConfig.getAspectpath().add(new File("anotherEntry.jar"));
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));
    }
    
    public void testRemoveEntryFromAspectpath() {
        newConfig.getAspectpath().remove(0);
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));               
    }
    
    public void testReorderAspectpath() {
        Object o = newConfig.getClasspath().remove(0);
        newConfig.getAspectpath().add(o);
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));                       
    }

    public void testAddEntryToInpath() {
        newConfig.getInpath().add(new File("anotherEntry"));
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));
    }
    
    public void testRemoveEntryFromInpath() {
        newConfig.getInpath().remove(0);
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));               
    }
    
    public void testReorderInpath() {
        Object o = newConfig.getClasspath().remove(0);
        newConfig.getInpath().add(o);
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));                       
    }
    
    public void testAddEntryToInjars() {
        newConfig.getInJars().add(new File("anotherEntry.jar"));
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));
    }
    
    public void testRemoveEntryFromInjars() {
        newConfig.getInJars().remove(0);
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));               
    }
    
    public void testReorderInjars() {
        Object o = newConfig.getClasspath().remove(0);
        newConfig.getInJars().add(o);
        assertFalse("Can do incremental",aRightState.prepareForNextBuild(newConfig));                       
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        aRightState = new AjState(null);
        oldConfig = new AjBuildConfig();
        newConfig = new AjBuildConfig();
        List cp = new ArrayList();
        cp.add("adir");
        cp.add("ajar.jar");
        oldConfig.setClasspath(cp);
        newConfig.setClasspath(new ArrayList(cp));
        List ap = new ArrayList();
        ap.add(new File("aLib.jar"));
        ap.add(new File("anotherLib.jar"));
        oldConfig.setAspectpath(ap);
        newConfig.setAspectpath(new ArrayList(ap));
        List ip = new ArrayList();
        ip.add(new File("adir"));
        ip.add(new File("ajar.jar"));
        oldConfig.setInPath(ip);
        newConfig.setInPath(new ArrayList(ip));
        List ij = new ArrayList();
        ij.add(new File("aLib.jar"));
        ij.add(new File("anotherLib.jar"));
        oldConfig.setInJars(ij);
        newConfig.setInJars(new ArrayList(ij));
        aRightState.prepareForNextBuild(oldConfig);
        aRightState.successfulCompile(oldConfig,true);
    }
}
