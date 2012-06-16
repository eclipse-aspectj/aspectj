import java.util.List;

class A extends SuperA<String> {
  public B getSomeB(SuperB<String> b) { return null; }
 public static void main(String []argv) {
    A a = new A();
	System.out.println(a.getSomeB(null));
  }
}

class B extends SuperB<String> { 
}

abstract class SuperA<T> {
	public abstract SuperB<T> getSomeB(SuperB<T> b);
}

class SuperB<T> { }

/*
public privileged aspect A_ITD {
	declare parents: A extends SuperA<String>;
	
	public B A.getSomeB(SuperB<String> b) { return null; }
}
*/
