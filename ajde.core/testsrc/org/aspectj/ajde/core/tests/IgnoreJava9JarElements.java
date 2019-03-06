/*******************************************************************************
 * Copyright (c) 2019 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.ajde.core.tests;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;

public class IgnoreJava9JarElements extends AjdeCoreTestCase {

    public static final String injarName = "build/libs/IgnoreJava9JarElements.jar";
    public static final String outjarName = "outjar.jar";

    private TestMessageHandler handler;
    private TestCompilerConfiguration compilerConfig;

    protected void setUp() throws Exception {
        super.setUp();
        initialiseProject("IgnoreJava9JarElements");
        handler = (TestMessageHandler) getCompiler().getMessageHandler();
        compilerConfig = (TestCompilerConfiguration) getCompiler()
                .getCompilerConfiguration();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        handler = null;
        compilerConfig = null;
    }

    public void testWeave() {
        Set<File> injars = new HashSet<File>();
        injars.add(openFile(injarName));
        compilerConfig.setInpath(injars);
        Set<File> aspectpath = new HashSet<File>();
        compilerConfig.setAspectPath(aspectpath);
        File outjar = openFile(outjarName);
        compilerConfig.setOutjar(outjar.getAbsolutePath());
        doBuild(true);
        assertTrue("Expected no compiler errors or warnings but found "
                + handler.getMessages(), handler.getMessages().isEmpty());
        compareManifests(openFile(injarName), openFile(outjarName));
    }

    private void compareManifests(File inFile, File outFile) {

        try {
            JarFile inJar = new JarFile(inFile);
            Manifest inManifest = inJar.getManifest();
            inJar.close();
            JarFile outJar = new JarFile(outFile);
            Manifest outManifest = outJar.getManifest();
            outJar.close();
            assertTrue("The manifests in '" + inFile.getCanonicalPath()
                    + "' and '" + outFile.getCanonicalPath()
                    + "' sould be the same", inManifest.equals(outManifest));
        } catch (IOException ex) {
            fail(ex.toString());
        }
    }

}
