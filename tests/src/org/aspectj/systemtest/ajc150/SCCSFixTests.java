/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wes Isberg - initial implementation
 *******************************************************************************/

package org.aspectj.systemtest.ajc150;

import java.io.File;

import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;
import org.aspectj.util.FileUtil;

/**
 * SCCS/CVS directory fix.
 * Would add to Ajc150TestsNoHarness, but can't share basedir/setup, etc.
 */
public class SCCSFixTests extends AjcTestCase {
    File baseDir;
    File sourceroot;

    public void setUp() throws Exception {
        super.setUp();
        baseDir = FileUtil.getTempDir("BugFixTests");
        sourceroot = new File(baseDir, "sourceroot");
        sourceroot.mkdirs();
    }
    public void tearDown() {
        FileUtil.deleteContents(baseDir);
    }
    /**
     * @see org/aspectj/util/FileUtil.java 1.17
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=48650
     */
    public void testSkipCVS() {
        doTestSkip("CVS");
    }

    /**
     * @see org/aspectj/util/FileUtil.java 1.17
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=48650
     */
    public void testSkipSCCS() {
        doTestSkip("SCCS");
    }

    /**
     * Can't check in "CVS" or "SCCS" directories,
     * so construct for each test.
     */
    private void doTestSkip(String name) {
        File dir = new File(sourceroot, name);
        sourceroot.mkdirs();
        File file = new File(dir, "Error.java");
        FileUtil.writeAsString(file, "public class Error { here }");
        file = new File(sourceroot, "Main.java");
        FileUtil.writeAsString(file, MAIN);
        String[] args = { "-sourceroots", sourceroot.getPath() };
        CompilationResult result = ajc(baseDir, args);
        assertNoMessages(result);
        RunResult r = run("Main"); 
        String m = r.getStdOut().trim();
        assertEquals("I ran", m);
    }
    private static final String MAIN =
        "public class Main { public static void main(String[] a) {System.out.println(\"I ran\");}}";
}
