// what about printing the join point?
public class Four {
  public static void main(String []argv) {
    Integer[] Is = new Integer[5];
    Foo f = new Foo(6);
  }
}

aspect X {
  before(): call(Integer[].new(int)) {
	  System.err.println("tjp1=>"+thisJoinPoint);
  }
  before(): call(Foo.new(int)) {
	  System.err.println("tjp2=>"+thisJoinPoint);
  }
}
class Foo {
	Foo(int i) {
		
	}
}
