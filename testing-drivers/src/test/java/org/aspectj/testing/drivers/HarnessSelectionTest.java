/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.drivers;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.harness.bridge.AbstractRunSpec;
import org.aspectj.testing.harness.bridge.AjcTest;
import org.aspectj.testing.harness.bridge.IRunSpec;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.RunValidator;
import org.aspectj.testing.util.BridgeUtil;
import org.aspectj.testing.util.RunUtils;
import org.aspectj.testing.xml.AjcSpecXmlReader;
import org.aspectj.util.LangUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import junit.framework.TestCase;

/**
 * The harness supports custom-coded queries based on
 * -ajctest{kind}={query} parameters
 * (until we move to an XML database with real queries).
 */
public class HarnessSelectionTest extends TestCase {
    private static final String TESTDATA = "../testing-drivers/testdata";
    private static final String INC_HARNESS_DIR 
        = TESTDATA + "/incremental/harness";
    private static final String SELECT 
        = INC_HARNESS_DIR + "/selectionTest.xml";
        
    /** @see testIncrementalSuite() */
    private static final String INCREMENTAL 
        = INC_HARNESS_DIR + "/suite.xml";

    private static final String TITLE_LIST_ONE 
        = INC_HARNESS_DIR + "/titleListOne.txt";

    private static final String TITLE_LIST_PLURAL 
        = INC_HARNESS_DIR + "/titleListPlural.txt";
    
    private static Hashtable SPECS = new Hashtable();    
    
    private static AjcTest.Suite.Spec getSpec(String suiteFile) {
        AjcTest.Suite.Spec result = (AjcTest.Suite.Spec) SPECS.get(suiteFile);
        if (null == result) {
            try {
                result = AjcSpecXmlReader.getReader().readAjcSuite(new File(suiteFile));
                SPECS.put(suiteFile, result);
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }        
        try {
            return (AjcTest.Suite.Spec) result.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace(System.err);
            assertTrue("clone failed: " + e, false);
            return null; // keep compiler happy
        }
    }
    
    private boolean verbose;

	public HarnessSelectionTest(String name) {
		super(name);
	}
    
    public void testFilesAvailable() {
        String[] files = new String[] {
            SELECT, INCREMENTAL, TITLE_LIST_ONE, TITLE_LIST_PLURAL
        };
		for (String file : files) {
			assertTrue(file, new File(file).canRead());
		}
    }

    public void testIncrementalSuite() {
        System.err.println("skipping testIncrementalSuite - too long");
        if (true) return;
        if (!eclipseAvailable()) {
            System.err.println("skipping test - eclipse classes not available");
            return;
        }
        String[] options = new String[] 
            { "!verbose", "!eclipse",
            };
        Exp exp = new Exp(6, 6, 0, 6, 0, 0, 0);
        checkSelection(INCREMENTAL, options, "INFIX IGNORED", exp);
    }
    
    public void testKeywordSelectionBoth() {
        if (!eclipseAvailable()) {
            System.err.println("skipping test - eclipse classes not available");
            return;
        }
        String[] options = new String[] 
            { "-ajctestRequireKeywords=requireKeyword", 
            "-ajctestSkipKeywords=skipKeyword,skipUnenforcedAjcLimit",
            "!verbose",
            "-eclipse",
            };
        Exp exp = new Exp(17, 1, 16, 1, 0, 0, 1);
        checkSelection(SELECT, options, "keyword skipKeyword was found", exp);
    }
    
    public void testKeywordSelectionRequire() {
        if (!eclipseAvailable()) {
            System.err.println("skipping test - eclipse classes not available");
            return;
        }
        String[] options = new String[] 
            { "-ajctestRequireKeywords=skipKeyword", 
            "!verbose",
            "-eclipse",
            };
        Exp exp = new Exp(17, 1, 16, 1, 0, 0, 16);
        checkSelection(SELECT, options, "keyword skipKeyword was not found", exp);
    }

    public void testKeywordSelectionSkip() {
        if (!eclipseAvailable()) {
            System.err.println("skipping test - eclipse classes not available");
            return;
        }
        String[] options = new String[] 
            { "-ajctestSkipKeywords=requireKeyword", 
            "!verbose",
            "-eclipse",
            };
        Exp exp = new Exp(17, 0, 17, 0, 0, 0, 17);
        checkSelection(SELECT, options, "keyword requireKeyword was found", exp);
    }
    
    public void testNoOptions() {
        if (!ajcAvailable()) {
            System.err.println("skipping test - ajc classes not available");
            return;
        }
        String[] options = new String[] 
            { "!ajc"
            };
        Exp exp = new Exp(17, 3, 14, 3, 0, 0, 4);
        checkSelection(SELECT, options, "normally-valid", exp);
    }

    public void testEclipseOptionsSkip() {
        String[] options = new String[] 
            { "-eclipse",
                "-ajctestRequireKeywords=eclipseOptionSkip"
            };
        Exp exp = new Exp(17, 0, 17, 0, 0, 0, 6);
        checkSelection(SELECT, options, "old ajc 1.0 option", exp);
    }
    
    public void testAjcEclipseConflict() {
        if (!ajcAvailable()) {
            System.err.println("skipping test - ajc classes not available");
            return;
        }
        String[] options = new String[] 
            { "!ajc"
            };
        Exp exp = new Exp(17, 3, 14, 3, 0, 0, 6);
        checkSelection(SELECT, options, "conflict between !eclipse and !ajc", exp);
    }
    
    public void testEclipseConflict() {
        String[] options = new String[] 
            { "^eclipse",
              "-ajctestSkipKeywords=skipUnenforcedAjcLimit"
            };            
        Exp exp = new Exp(17, 3, 14, 3, 0, 0, 6);
        checkSelection(SELECT, options, "force conflict between eclipse", exp);
    }
    
    public void testSinglePR() {
        String[] options = new String[] 
            { "-eclipse", "-ajctestPR=100"
            };
        Exp exp = new Exp(17, 1, 16, 1, 0, 0, 16);
        checkSelection(SELECT, options, "bugId required", exp);
    }
    
    public void testTwoPR() {
        String[] options = new String[] 
            { "-eclipse", "-ajctestPR=100,101"
            };
        Exp exp = new Exp(17, 2, 15, 2, 0, 0, 15);
        checkSelection(SELECT, options, "bugId required", exp);
    }
    
    public void testTitleContainsSubstringSelection() {
        String[] options = new String[] 
            { "-ajctestTitleContains=run and ",
                "-eclipse" 
            };
        Exp exp = new Exp(17, 1, 16, 1, 0, 0, 16);
        checkSelection(SELECT, options, "run and", exp);
    }
    
    public void testTitleContainsSubstringSelectionPlural() {
        String[] options = new String[] 
            { "-ajctestTitleContains= run and , if skipKeyword ",
                "-eclipse" 
            };
        Exp exp = new Exp(17, 2, 15, 2, 0, 0, 15);
        checkSelection(SELECT, options, "title", exp);
    }

    public void testTitleContainsExactSelection() {
        String[] options = new String[] 
            { "-ajctestTitleContains=run and pass",
                "-eclipse" 
            };
        Exp exp = new Exp(17, 1, 16, 1, 0, 0, 16);
        checkSelection(SELECT, options, "run and pass", exp);
    }
    
    public void testTitleContainsExactSelectionPlural() {
        String[] options = new String[] 
            { "-ajctestTitleContains= run and pass , omit if skipKeyword ",
                "-eclipse" 
            };
        Exp exp = new Exp(17, 2, 15, 2, 0, 0, 15);
        checkSelection(SELECT, options, "title", exp);
    }

    public void testTitleListSelection() {
        String[] options = new String[] 
            { "-ajctestTitleList=run and pass",
                "-eclipse" 
            };
        Exp exp = new Exp(17, 1, 16, 1, 0, 0, 16);
        checkSelection(SELECT, options, "run and pass", exp);
    }
    
    public void testTitleListSelectionPlural() {
        String[] options = new String[] 
            { "-ajctestTitleList= run and pass , omit if skipKeyword ",
                "-eclipse" 
            };
        Exp exp = new Exp(17, 2, 15, 2, 0, 0, 15);
        checkSelection(SELECT, options, "title", exp);
    }

    public void testTitleListFileSelection() {
        String[] options = new String[] 
            { "-ajctestTitleList=" + TITLE_LIST_ONE,
                "-eclipse"
            };
        Exp exp = new Exp(17, 1, 16, 1, 0, 0, 16);
        checkSelection(SELECT, options, TITLE_LIST_ONE, exp);
    }

   public void testTitleListFileSelectionPlural() {
       String[] options = new String[] 
           { "-ajctestTitleList=" + TITLE_LIST_PLURAL,
               "-eclipse"
           };
       Exp exp = new Exp(17, 2, 15, 2, 0, 0, 15);
       checkSelection(SELECT, options, TITLE_LIST_PLURAL, exp);
       
       // Now check the "fail only" path 
       options = new String[] 
           { "-ajctestTitleFailList=" + TITLE_LIST_PLURAL,
               "-eclipse"
           };
       // 1 messages skipped when run under 1.4 for other reasons,
       // so count "skip" instead of TITLE_LIST_PLURAL
       exp = new Exp(17, 2, 15, 2, 0, 0, 15);
       checkSelection(SELECT, options, "skip", exp);
   }

    /** 
     * Run the static test suite with the given options.
     * @param setupHolder the IMessageHolder for any setup messages
     * @return null if setup failed or Harness.RunResult if suite completed.
     */
    private Harness.RunResult runSuite(String suiteFile, String[] options, MessageHandler setupHolder) {
        AbstractRunSpec.RT runtime = new AbstractRunSpec.RT();
        runtime.setOptions(options);
        AjcTest.Suite.Spec spec = getSpec(suiteFile);
        assertNotNull(spec);
        ArrayList<IRunSpec> kids = spec.getChildren();
        assertNotNull(kids);
        if ((suiteFile == SELECT) && (17 != kids.size())) {
            assertTrue("expected 17 kids, got " + kids.size(), false);
        }
        if (!spec.adoptParentValues(runtime, setupHolder)) {
            return null; 
        } else {
            class TestHarness extends Harness {
                public RunResult run(AjcTest.Suite.Spec spec) {
                    return super.run(spec);
                } 
            }
            TestHarness h = new TestHarness();
            return h.run(spec);            
        }
    }

    class Exp {
        public final int tests;
        public final int testsRun;
        public final int skipped;
        public final int passed;
        public final int failed;
        public final int incomplete;
        public final int infix;
        Exp(int tests, int testsRun, int skipped, int passed, int failed, int incomplete, int infix) {
            this.tests = tests;
            this.testsRun = testsRun;
            this.skipped = skipped;
            this.passed = passed;
            this.failed = failed;
            this.incomplete = incomplete;
            this.infix = infix;
        }
    }   
         
    public void checkSelection(String suiteFile, String[] options, String infoInfix, Exp exp) {
        MessageHandler holder = new MessageHandler();
        Harness.RunResult result = runSuite(suiteFile, options, holder);
        if (verbose) {
            MessageUtil.print(System.out, holder, " setup - ");
        }
        assertNotNull("Harness.RunResult", result);
        // XXX sync hack snooping of message text with skip messages, harness
        final List skipList = MessageUtil.getMessages(holder, IMessage.INFO, false, "skip");
        final int numSkipped = skipList.size();        
        IRunStatus status = result.status;
        assertNotNull(status);
        if (verbose) {
            RunUtils.print(System.out, "result - ", status);
            System.out.println(BridgeUtil.childString(status, numSkipped, result.numIncomplete));
        }
        assertEquals("skips", exp.skipped, numSkipped);
        IRunStatus[] children = status.getChildren();
        assertNotNull(children);
        assertTrue(children.length + "!= expRun=" + exp.testsRun, 
            exp.testsRun == children.length);
        int actPass = 0;
		for (IRunStatus child : children) {
			if (RunValidator.NORMAL.runPassed(child)) {
				actPass++;
			}
		}
        if (exp.passed != actPass) {
            assertTrue("exp.passed=" + exp.passed + " != actPass=" + actPass, false);
        }
        if (!LangUtil.isEmpty(infoInfix)) {
            int actInfix = MessageUtil.getMessages(holder, IMessage.INFO, false, infoInfix).size();
            if (actInfix != exp.infix) {
                String s = "for infix \"" + infoInfix 
                    + "\" actInfix=" + actInfix + " != expInfix=" + exp.infix;
                assertTrue(s, false);
            }
        }
    }
    
    private boolean ajcAvailable() { // XXX util
        try {
            return (null != Class.forName("org.aspectj.compiler.base.JavaCompiler"));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    
    private boolean eclipseAvailable() { // XXX util
        try {
            return (null != Class.forName("org.aspectj.ajdt.ajc.AjdtCommand"));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
