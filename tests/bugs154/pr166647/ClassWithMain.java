package a;

public class ClassWithMain {
	public static void main(String []argv) {
		new ClassToAdvise().toplevel();
		new ClassToAdvise().foo();
	}
}