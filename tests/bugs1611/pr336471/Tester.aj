
aspect Tester {
	before(): call(* Foo.*(..)) {
		System.out.println(thisEnclosingJoinPointStaticPart);
	}
	public static void main(String[] args) {
		new Foo().run();
	}
}

class Foo {
	int i = 5;
	public void run() {
		bar("abc");
	}
	
	public void bar(String s) {
		System.out.println("hello");
		i = 4;
	}
}

class MyException extends Exception {
	
}
