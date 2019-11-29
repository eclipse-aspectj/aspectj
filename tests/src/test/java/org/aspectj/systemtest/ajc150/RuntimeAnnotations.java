package org.aspectj.systemtest.ajc150;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 *  Checking that runtime visible annotations are visible at runtime (they get into the class file)
 */
public class RuntimeAnnotations extends XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(RuntimeAnnotations.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc150.xml");
	}

	public void test01() {
		runTest("public method with declare @method");
	}

	public void test02() {
		runTest("public method on the aspect that declares @method on it");
	}

	public void test03() {
		runTest("public annotated method");
	}

	public void test04() {
		runTest("public ITD method with declare @method");
	}

	public void test05() {
		runTest("public annotated ITD method");
	}

	public void test06() {
		runTest("public ITD-on-itself method with declare @method");
	}

	public void test07() {
		runTest("public annotated ITD-on-itself method");
	}

	public void test08() {
		runTest("public method on an Interface with declare @method");
	}

	public void test09() {
		runTest("public annotated method on an Interface");
	}

	public void test10() {
		runTest("public ITD method onto an Interface with declare @method");
	}

	public void test11() {
		runTest("public annotated ITD method onto an Interface");
	}

	public void test12() {
		runTest("public abstract method with declare @method");
	}

	public void test13() {
		runTest("public abstract method on the aspect that declares @method on it");
	}

	public void test14() {
		runTest("public abstract annotated method");
	}

	public void test15() {
		runTest("public abstract ITD method with declare @method");
	}

	public void test16() {
		runTest("public abstract annotated ITD method");
	}

	public void test17() {
		runTest("public abstract ITD-on-itself method with declare @method");
	}

	public void test18() {
		runTest("public abstract annotated ITD-on-itself method");
	}

	public void test19() {
		runTest("public abstract method on an Interface with declare @method");
	}

	public void test20() {
		runTest("public abstract annotated method on an Interface");
	}

	public void test21() {
		runTest("public abstract ITD method onto an Interface with declare @method");
	}

	public void test22() {
		runTest("public abstract annotated ITD method onto an Interface");
	}

	public void test23() {
		runTest("public field with declare @field");
	}

	public void test24() {
		runTest("public field on the aspect that declares @field on it");
	}

	public void test25() {
		runTest("public annotated field");
	}

	public void test26() {
		runTest("public ITD field with declare @field");
	}

	public void test27() {
		runTest("public annotated ITD field");
	}

	public void test28() {
		runTest("public ITD-on-itself field with declare @field");
	}

	public void test29() {
		runTest("public annotated ITD-on-itself field");
	}

}
