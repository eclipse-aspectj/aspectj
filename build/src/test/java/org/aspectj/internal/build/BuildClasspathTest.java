/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wes Isberg       initial implementation
 * ******************************************************************/

package org.aspectj.internal.build;

import org.aspectj.internal.tools.build.Result;
import org.aspectj.internal.tools.build.Result.Kind;

import junit.framework.TestCase;

public class BuildClasspathTest extends TestCase {

    public void testKindsGet() {
        Kind kind = Result.kind(Result.NORMAL, Result.ASSEMBLE);
        same(kind, "RELEASE_ALL");
        kind = Result.kind(Result.NORMAL, !Result.ASSEMBLE);
        same(kind, "RELEASE");
        kind = Result.kind(!Result.NORMAL, Result.ASSEMBLE);
        same(kind, "TEST_ALL");
        kind = Result.kind(!Result.NORMAL, !Result.ASSEMBLE);
        same(kind, "TEST");
    }
    private void same(Kind kind, String name) {
        if (!name.equals(kind.toString())) {
            fail("expected \"" + name + "\" got \"" + kind + "\"");
        }
    }
//    public void testClasspath() {
//        Messager handler = new Messager();
//        File baseDir = new File("..");
//        File jarDir = new File("../aj-build/jars");
//        Modules modules = new Modules(baseDir, jarDir, handler);
//        Module module = modules.getModule("ajbrowser");
//        Kind kind = Result.kind(Result.NORMAL, !Result.ASSEMBLE);
//        Result result = module.getResult(kind);
//        print(result);
//    }
//    public void testBuildClasspath() {
//        Messager handler = new Messager();
//        File baseDir = new File("..");
//        File jarDir = new File("../aj-build/jars");
//        Modules modules = new Modules(baseDir, jarDir, handler);
//        Module module = modules.getModule("build");
//        Kind kind = Result.kind(Result.NORMAL, !Result.ASSEMBLE);
//        Result result = module.getResult(kind);
//        print(result);
//    }
    private void print(Result result) {
        System.out.println(result + " libjars" + result.getLibJars());
        System.out.println(result + " required" + result.getRequired());
    }
}
