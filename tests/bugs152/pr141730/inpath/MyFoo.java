package foo;

public class MyFoo {

	public int i;
	
	public MyFoo() {
		super();
	}
	
	public MyFoo(String s) {
		super();
	}
	
	public void callMain() {
		new MyFoo().main();
	}
	
	public void main() {
		System.out.println("blah");
	}
	
	public void anotMethod() {	
	}
}
