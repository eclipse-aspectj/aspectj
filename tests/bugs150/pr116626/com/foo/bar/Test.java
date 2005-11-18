package com.foo.bar;

import java.util.*;

public class Test<T> {

	Set<T> intsSet;

	public Test() {
		this.intsSet = new HashSet<T>();
	}

	public <T> T[] getObjs(T[] a) {
		return intsSet.toArray(a);
	}

	public static void main(String[] args) {
		System.out.println("AAA :-)");
		new TTT().foo();
	}
}

class TTT {
	public void foo() {
		Test<Object> mt = new Test<Object>();
		Object[] arr = mt.getObjs(new Object[]{});
	}
}
