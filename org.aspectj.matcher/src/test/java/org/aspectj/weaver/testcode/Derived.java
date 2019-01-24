package org.aspectj.weaver.testcode;

import java.io.IOException;

public class Derived extends Base {

	public static void onlyDerived() throws IOException, CloneNotSupportedException {
	}

	public static void both() {
	}

	public void onlyDerivedNonStatic() {
	}

	public void bothNonStatic() {
	}

	public int onlyDerived;
	public int both;

	public Derived() {
	}

	public void m() {
	}

}
