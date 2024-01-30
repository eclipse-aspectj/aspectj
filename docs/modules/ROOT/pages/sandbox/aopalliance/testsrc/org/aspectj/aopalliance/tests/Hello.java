
package org.aspectj.aopalliance.tests;

public class Hello {
	
	public static boolean defaultConsExecuted = false;
	public static boolean paramConsExecuted = false;
	public static int sayHelloCount = 0;
	
	private String msg = "Hello";
	
	public Hello() { defaultConsExecuted = true;}
	
	public Hello(String s) {
		msg = s;
		paramConsExecuted = true;
	}
	
	public String sayHello() {
		sayHelloCount++;
		return msg;
	}
	
}
