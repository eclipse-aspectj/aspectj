package main.p1;

import main.p2.AbstractTest;
import main.Driver;

final aspect ConcreteTest extends AbstractTest {

	protected pointcut pc(): execution(* Driver.doStuff());

	protected pointcut pc2(): execution(* Driver.doOtherStuff());

	Object around(): pc2() {
		System.out.println("adding to the other stuff");
		/*If we comment out the next line we don't get a verify error.*/
		System.out.println("The value of the field when replacing is " + getField());
		return proceed();
	}

	protected void hook() {
		/*This doesn't cause a verify error seemably because the advice calling it is in AbstractTest*/
		System.out.println("The value of the field is " + getField());
	}
}