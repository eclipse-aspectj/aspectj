import org.aspectj.weaver.TestJava5ReflectionBasedReferenceTypeDelegate;
import org.aspectj.weaver.patterns.ArgsTestCase;
import org.aspectj.weaver.patterns.ThisOrTargetTestCase;
import org.aspectj.weaver.tools.Java15PointcutExpressionTest;
import org.aspectj.weaver.tools.PointcutExpressionTest;
import org.aspectj.weaver.tools.PointcutParserTest;
import org.aspectj.weaver.tools.TypePatternMatcherTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllWeaver5Tests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for default package");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestJava5ReflectionBasedReferenceTypeDelegate.class);
		suite.addTestSuite(Java15PointcutExpressionTest.class);
		suite.addTestSuite(ArgsTestCase.class);
		suite.addTestSuite(ThisOrTargetTestCase.class);
		suite.addTestSuite(PointcutExpressionTest.class);
		suite.addTestSuite(PointcutParserTest.class);
		suite.addTestSuite(TypePatternMatcherTest.class);
		//$JUnit-END$
		return suite;
	}

}
