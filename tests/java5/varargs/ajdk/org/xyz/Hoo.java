package org.xyz;

public class Hoo {
	
	public Hoo(Foo... foos) {}
	public Hoo(Goo... goos) {}
	public Hoo(String... ss) {
		intStringVar(5,ss);
		intString(5,ss[0]);
	}
	
	void intStringVar(int i, String... ss) {}
	void intString(int i, String s) {}
	void integerVar(Integer... is) {}
	
}