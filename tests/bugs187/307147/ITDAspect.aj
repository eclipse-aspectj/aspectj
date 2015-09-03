package test;

import test.Test;

public privileged aspect ITDAspect {
	public void Test.itdFunction() {
		System.out.println("ITD function");
		privateMethod();
		publicMethod();
	}
}
