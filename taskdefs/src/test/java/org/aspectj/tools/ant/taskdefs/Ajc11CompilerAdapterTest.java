/* *******************************************************************
 * Copyright (c) 2003 Contributors. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ant.taskdefs;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;
import org.aspectj.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * 
 */
public class Ajc11CompilerAdapterTest extends TestCase {
    public static boolean LOGGING = false;
    List tempFiles = new ArrayList();
    
    public Ajc11CompilerAdapterTest(String name) {
        super(name);
    }
    
    public void tearDown() {
		for (Object tempFile : tempFiles) {
			File file = (File) tempFile;
			FileUtil.deleteContents(file);
			file.delete();
		}
    }
    
//    public void testCompilerAdapterWithJavac() { // XXX requires tools.jar 
//        Javac javac = getJavac(new Project());
//        setupTracingJava(javac);
//        javac.execute();
//    }

    public void testCompilerAdapterWithAjc() { // XXX unverified        
        Project project = new Project();
        String cname = Ajc11CompilerAdapter.class.getName();
        project.setProperty("build.compiler", cname);
        Javac javac = getJavac(project);
        setupTracingJava(javac);
        log("---- first compile...");
        System.out.flush();
        javac.execute();
        log("---- second compile (none: nothing out of date?)...");
        javac.execute(); 
    }

    public void testCompilerAdapterWithAjcRecursively() { // XXX unverified
        Project project = new Project();
        String cname = Ajc11CompilerAdapter.class.getName();
        project.setProperty("build.compiler", cname);
        project.setProperty("build.compiler.clean", "yes");
        Javac javac = getJavac(project);
        setupTracingJava(javac);
        log("---- first compile...");
        System.out.flush();
        javac.execute();
        log("---- second compile...");
        System.out.flush();
        javac.execute(); // expecting full recompile - pre-cleaned
    }

    Javac getJavac(Project project) {
        Javac javac = new Javac();
        javac.setProject(project);
        File classesDir = FileUtil.getTempDir("Ajc11CompilerAdapterTest");
        tempFiles.add(classesDir);
        javac.setDestdir(classesDir);
        javac.setVerbose(LOGGING);
        String rtpath = "../lib/test/aspectjrt.jar";
        File rt = new File(rtpath);
        assertTrue("can read " + rtpath, rt.canRead());
        javac.setClasspath(new Path(project, rt.getAbsolutePath()));
        return javac;
    }
    
    void setupTracingJava(Javac javac) { // XXX assumes module dir, doc loc
        String exDir = "../docs/dist/doc/examples"; 
        javac.setSrcdir(new Path(javac.getProject(), exDir));
        javac.setIncludes("tracing/*.java"); // XXX assumes tracing example
    }
    void log(String s) {
        if (LOGGING) {
            System.err.println(s);
        }
    }
}
