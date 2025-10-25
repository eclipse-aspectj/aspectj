package org.aspectj.systemtest.ajc1925;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

/*
 * Some very trivial tests that help verify things are OK.
 * These are a copy of the earlier Sanity Tests created for 1.6 but these supply the -23 option
 * to check code generation and modification with that version specified.
 *
 * @author Andy Clement
 */
public class SanityTestsJava25 extends JavaVersionSpecificXMLBasedAjcTestCase {

	public static final int bytecode_version_for_JDK_level = Constants.ClassFileVersion.of(25).MAJOR;

	public SanityTestsJava25() {
		super(25);
	}

	// Incredibly trivial test programs that check the compiler works at all (these are easy-ish to debug)
	public void testSimpleJava_A() {
		runTest("simple - a");
	}

	public void testSimpleJava_B() {
		runTest("simple - b");
	}

	public void testSimpleCode_C() {
		runTest("simple - c");
	}

	public void testSimpleCode_D() {
		runTest("simple - d");
	}

	public void testSimpleCode_E() {
		runTest("simple - e");
	}

	public void testSimpleCode_F() {
		runTest("simple - f");
	}

	public void testSimpleCode_G() {
		runTest("simple - g");
	}

	public void testSimpleCode_H() {
		runTest("simple - h", true);
	}

	public void testSimpleCode_I() {
		runTest("simple - i");
	}

	public void testVersionCorrect1() {
		runTest("simple - j");
		checkVersion("A", bytecode_version_for_JDK_level, 0);
	}

	public void testVersionCorrect2() {
		runTest("simple - k");
		checkVersion("A", bytecode_version_for_JDK_level, 0);
	}

	public void testVersionCorrect4() {
		runTest("simple - m");
		checkVersion("A", Constants.ClassFileVersion.of(8).MAJOR, 0);
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(SanityTestsJava25.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("sanity-tests-25.xml");
	}

}
