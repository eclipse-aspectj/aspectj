
package foo;

import java.io.IOException;

/**
 * Test class.  This is a comment.
 */  
public abstract class ClassA implements InterfaceI {
	
	/**
	 * Mumble field.
	 */
	public String mumble = "xxx";
	public int pubfield;
	private String privfield = "mumble";
	 
	public IOException exception = new IOException() {
		
	    public String getMumble() { return "mumble"; }
	};
	 
	/**
	 * Mumbo. Jumbo. 
	 * 
	 * @param arg1  integer parameter
	 */ 
	void method1(int arg1) throws IOException { 
		pubfield = arg1;
	}
	
	public void foo() { }
	
	static aspect InnerAspect {
		String s;
	}
}
 
class SubClass extends ClassA { 
	 
	public void foo() { }
}