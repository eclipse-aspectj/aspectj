import org.aspectj.systemtest.AllTests14;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.AssertionFailedError;
import junit.framework.TestResult;
import junit.framework.TestListener;
import junit.textui.TestRunner;
import junit.textui.ResultPrinter;
import junit.extensions.TestDecorator;

import java.util.Iterator;

/*
 * Created on 03-Aug-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RunTheseBeforeYouCommitTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for default package");
		//$JUnit-BEGIN$
		suite.addTest(AllTests.suite());
		suite.addTest(AllTests14.suite());
		//$JUnit-END$
		return suite;
	}

    public static void main(String args[]) throws Throwable {
//        TestRunner.run(suite());
        junit.textui.TestRunner runner = new TestRunner();

        // AV - for odd reason I could not have a proper error reporting
        runner.doRun(new TestDecorator(suite()) {
            public void run(final TestResult testResult) {
                testResult.addListener(new TestListener() {
                    public void addError(Test test, Throwable throwable) {
                        System.err.println("******* ERROR ");
                        System.err.println("\t" + test);
                        throwable.printStackTrace();
                    }

                    public void addFailure(Test test, AssertionFailedError assertionFailedError) {
                        System.err.println("******* FAILURE ");
                        System.err.println("\t" + test);
                        assertionFailedError.printStackTrace();
                    }

                    public void endTest(Test test) {
                    }

                    public void startTest(Test test) {
                        System.out.println("->" + test);
                    }
                });
                super.run(testResult);
            }
        });
    }

}
