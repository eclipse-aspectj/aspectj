package pkg;

public class C {
	
	public void method(){
		new C().foo();
		new C().goo();
	}
	
	public void foo() { }
	public void goo() { }
}
