package main.p1;

import main.p2.AbstractTest;
import main.Driver;

final aspect ConcreteTest extends AbstractTest {

	protected pointcut pc(): execution(* Driver.doStuff());

	protected pointcut pc2(): execution(* Driver.doOtherStuff());

	Object around(): pc2() {
		//System.out.println("adding to the other stuff");
		/*If we comment out the next line we don't get a verify error.*/
		ConcreteTest ct = this;
		System.out.println("test: " + s + ", " + this.s + ", " + ct.s);
		System.out.println("The value of the field when replacing is " + getField());
		return proceed();
	}

	protected void hook() {
		/*This doesn't cause a verify error because this code is not inlined*/
		System.out.println("The value of the field is " + getField());
	}
}