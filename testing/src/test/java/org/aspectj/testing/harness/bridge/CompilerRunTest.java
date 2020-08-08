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

package org.aspectj.testing.harness.bridge;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.RunStatus;
import org.aspectj.testing.run.Runner;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

import junit.framework.TestCase;

/**
 * Use a stub compiler/ICommand to verify command-line passed
 * to the compiler by the harness.
 */
public class CompilerRunTest extends TestCase {

    /** String for each dummy report: {run|repeat}: [{args}] */
    private static ArrayList dummyReports = new ArrayList();

    private static void dummyRunning(String[] args) {
        dummyReports.add("run: " + Arrays.asList(args));
    }
    
//    private static void dummyRepeating(String[] args) {
//        dummyReports.add("repeat: " + Arrays.asList(args));
//    }

    private File testBaseDir;
        
    public CompilerRunTest(String name) {
        super(name);
    }
    
    public void setUp() {
        testBaseDir = new File("../testing/temp-CompilerRunTest");
        File f = new File(testBaseDir, "one");
        f.mkdirs();
        assertTrue(f.canRead());
        f = new File(testBaseDir, "two");
        f.mkdirs();
        assertTrue(f.canRead());
        f = new File(testBaseDir, "Foo.java");
        String foo = "public class Foo { public void main(String[] s) { System.out.println(\"Hello!\");}}";
        String err = FileUtil.writeAsString(f, foo);
        assertTrue(err, null == err);
        assertTrue(f.canRead());
    }
    
    public void tearDown() {
        FileUtil.deleteContents(testBaseDir);
        testBaseDir.delete();
        testBaseDir = null;
    }
    
    public void testExtDirs() {
//        String[] globals = null;
        CompilerRun.Spec spec = new CompilerRun.Spec();
        spec.setExtdirs("one,two"); 
        spec.setFiles("Foo.java");
        checkCommandLine(testBaseDir, spec, null, "-extdirs");
    }

    void checkCommandLine(        
        File testBaseDir,
        CompilerRun.Spec spec, 
        String[] globals,
        String expectedInCommand) {
        assertTrue(0 == dummyReports.size());
        assertTrue(checkCompilerRun(testBaseDir, spec, globals, null));
        assertTrue(dummyReports.toString(), 1 == dummyReports.size());
        String command = (String) dummyReports.remove(0);
        assertTrue(0 == dummyReports.size());
        if ((null == command) 
            || (!command.contains(expectedInCommand))) {
            assertTrue("expected " 
                + expectedInCommand 
                + "got "
                + command, 
                false);
        }
    }

    /** run with dummy compiler */
    boolean checkCompilerRun(
        File testBaseDir,
        CompilerRun.Spec spec, 
        String[] globals,
        MessageHandler handler) {
        LangUtil.throwIaxIfNull(spec, "spec");
        if (null == handler) {
            handler = new MessageHandler();
        }
        spec.setPermitAnyCompiler(true);
        spec.setCompiler(DummyCompiler.class.getName());
        AbstractRunSpec.RT parentRuntime = new AbstractRunSpec.RT();

        if (!LangUtil.isEmpty(globals)) {
            parentRuntime.setOptions(globals);
        }
        boolean adopted =
            spec.adoptParentValues(parentRuntime, handler);
        if (!adopted) {
            String s = "not adopted spec="
                    + spec
                    + " globals="
                    + (LangUtil.isEmpty(globals)
                        ? "[]"
                        : Arrays.asList(globals).toString())
                    + " -- "
                    + handler;
            assertTrue(s, false);
        }
        if (0 != handler.numMessages(null, true)) {
            assertTrue("unexpected " + handler, false);
        }
        return run(testBaseDir, spec);
    }
    
    /** Run the compiler run specified by the spec */
    protected boolean run(File testBaseDir, CompilerRun.Spec spec) {

//        his is created using the Spec.</li>
//         * <li>setupAjcRun(Sandbox, Validator) is invoked,
//         *     at which point this populates the shared sandbox
//         *     with values derived from the spec and also
//         *     sets up internal state based on both the sandbox
//         *     and the spec.</li>
//         * <li>run(IRunStatus) is invoked,

        LangUtil.throwIaxIfNull(spec, "spec");
        Runner runner = new Runner();
        IMessageHolder holder = new MessageHandler();
        RunStatus status = new RunStatus(holder, runner);
        status.setIdentifier(spec);
        Validator validator = new Validator(status);
        validator.lock(this);
        Sandbox sandbox = null;
        try {
            sandbox = new Sandbox(testBaseDir, validator);
            IRunIterator test = spec.makeRunIterator(sandbox, validator);
            return runner.runIterator(test, status, null);
        } finally {
            validator.unlock(this);
            validator.deleteTempFiles(true);
        }
    }
 
    
    public static class DummyCompiler implements ICommand {
        private String[] command;
        
        public DummyCompiler() {
        }
        
        public boolean runCommand(
            String[] args,
            IMessageHandler handler) {
            command = (String[]) LangUtil.safeCopy(args, new String[0]);
            CompilerRunTest.dummyRunning(command);
            return true;
        }
        
        public boolean repeatCommand(IMessageHandler handler) {
            CompilerRunTest.dummyRunning(command);
            return true;
        }
    }
}
