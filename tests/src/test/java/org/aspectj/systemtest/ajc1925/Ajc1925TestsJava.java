package org.aspectj.systemtest.ajc1925;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc1925TestsJava extends JavaVersionSpecificXMLBasedAjcTestCase {

	private static final Constants.ClassFileVersion classFileVersion = Constants.ClassFileVersion.of(25);

	public Ajc1925TestsJava() {
		super(25);
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1925TestsJava.class);
	}
	
	// JEP 513: https://openjdk.org/jeps/513
	public void testFlexibleConstructorBodies() {
		runTest("flexible constructors - 1");
	}

	public void testFlexibleConstructorBodiesWithBeforeExecutionAdvice() {
		runTest("flexible constructors - 2");
	}

	public void testFlexibleConstructorBodiesWithBeforePreinitializationAdvice() {
		runTest("flexible constructors - 3");
	}

	public void testFlexibleConstructorBodiesWithBeforeInitializationAdvice() {
		runTest("flexible constructors - 4");
	}
	
	// JEP 512: https://openjdk.org/jeps/512
	public void testCompactSourceFiles() {
		runTest("compact source files - 1");
	}

	public void testCompactSourceFilesWithAdvice() {
		runTest("compact source files - 2");
	}

	public void testJep455PrimitivePatternsSwitch2() {
		runTest("primitive types patterns - switch - with advice");
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1925.xml");
	}

}
