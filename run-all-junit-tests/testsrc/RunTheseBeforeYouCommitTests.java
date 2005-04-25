import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.systemtest.AllTests15;

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
		suite.addTest(AllTests15.suite());
		//$JUnit-END$
		return suite;
	}
}
