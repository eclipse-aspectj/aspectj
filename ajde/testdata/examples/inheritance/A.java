
package inheritance;

public abstract class A {

	public abstract void bar();

	public void foo() { }
	
	public String toString() { 
		// mumble

		return ""; 
	} 
}

class B extends A {
	
	public void bar() { }
	
	public void foo() { }
}
