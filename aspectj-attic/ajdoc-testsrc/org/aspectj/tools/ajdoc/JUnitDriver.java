package org.aspectj.tools.ajdoc;
import junit.framework.Test;
import junit.framework.Assert;
import junit.textui.TestRunner;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

import org.aspectj.tools.ajdoc.Main;

/** 
 * Test driver for ajdoc
 * currently only has disabled test cases to invoke ajdoc
 * but not verify results.
 * @deprecated org.aspectj.testing.harness.AjdocScript
 */
public class JUnitDriver extends TestCase {
	private static final String[] ME 
		= new String[] {"org.aspectj.tools.ajdoc.JUnitDriver"};
	static final String ajbase = "c:/home/wes/aj";
	static final String testbase = ajbase + "/aj-build-modules/tests/ajdoc/JUnitDriver";
	static final String srcbase = ajbase + "/aspectj/modules/ajdoc/testsrc";

	private AjdocTestCase[] CASES;
	
	protected void setUp() {
	    assertTrue(null == CASES);
	    System.setProperty("seetag.debug", "on");
		CASES = new AjdocTestCase[]
			{ // both disabled as samples not checked in
			// getLinkTestCase()
			//, getJUnitTestCase()
			};
	}

	AjdocTestCase getLinkTestCase() {
		String outDir = testbase + "/link/api";

		new File(outDir).mkdirs();	
		return new AjdocTestCase("Link", new String[] 
			{
			  "-d", outDir
			, "-private"
			, "-sourcepath", srcbase
			, "test"   // doc test package only
			});
	}

	AjdocTestCase getJUnitTestCase() {
		String outDir =  "c:/home/doc/junit/api";

		new File(outDir).mkdir();	
		return new AjdocTestCase("JUnit", new String[] 
			{
			  "-d", outDir
			, "-private"
			, "-sourcepath"
			, "c:/home/doc/junit/src"
			, "junitjunit.awtui"
			, "junit.extensions"
			, "junit.framework"
			, "junit.runner"
			, "junit.swingui"
			, "junit.swingui.icons"
			, "junit.textui"
			, "junit.ui"
			});
	}
	
	public static void main(String[] args) {
		
		TestRunner.main(ME);
	}

    /** todo result logging? */
    public static void log(String s) {
        System.err.println(""+s);
    }

    /** load all KNOWN_TEST_CLASSES */
    public static Test suite() { 
        TestSuite result = new TestSuite();
        result.addTestSuite(JUnitDriver.class);
        return result;
    }

    //------------------ instance members 
    public JUnitDriver(String name) { super(name); }

	/**
	 * Run known test cases in CASES
	 */
    public void testAll() {
	    assertTrue(null != CASES);
    	for (int i = 0; i < CASES.length; i++) {
			CASES[i].run(this);
    	}
    	
    }
    /** this just invokes AJDoc but does not verify results */
    static class AjdocTestCase {
        private final String label;
    	public final String[] args;
        public AjdocTestCase(String label, String[] args) {
			this.label = (null == label ? "no label" : label);
			this.args = (null == args? new String[] {"help"} : args);
        }
    	public void run(Assert assert) {
    		int result = Main.execute(args);		
    		assert.assertTrue("result: " + result,0 == result);
    		// now verify...
    	}
    }

}  

