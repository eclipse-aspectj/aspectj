
package foo;

import java.io.IOException;

/**
 * Test class.  This is a comment.
 */ 
public abstract class ClassA implements InterfaceI {
	
	public int pubfield;
	private int privfield;
	
	/**
	 * Mumbo. Jumbo. 
	 * 
	 * @param arg1  integer parameter
	 */ 
	void method1(int arg1) throws IOException { 
		pubfield = arg1;
	}
	
	public void foo() { }
}
 
class SubClass extends ClassA { 
	 
	public void foo() { }
}