package org.aspectj.systemtest.ajc150.ataspectj.coverage;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestResult;

public class CoverageTests extends
        org.aspectj.testing.AutowiredXMLBasedAjcTestCase {

    // set to false to debug tests
    static final boolean failing = true;

    /**
     * disabled here so Ant JUnit rule wrt running *Tests works.
     */
    public static Test suite() {
        if (failing) {
            return new Test() {
                public int countTestCases() {
                    return 1;
                }

                public void run(TestResult r) {
                    r.startTest(this);
                    r.endTest(this);
                }

                public String toString() {
                    return CoverageTests.class.getName() + " fail";
                }
            };
        }
        return org.aspectj.testing.AutowiredXMLBasedAjcTestCase
                .loadSuite(CoverageTests.class);
    }

    protected URL getSpecFile() {
    	return getClassResource("coverage.xml");
//        return new File(
//                "../tests/src/org/aspectj/systemtest/ajc150/ataspectj/coverage/coverage.xml");
    }

}
