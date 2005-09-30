
abstract class A {}

class B extends A {}

aspect X {
	abstract String A.getName();
	public	String B.getName() { return "B"; }
	public String A.toString() { return getName(); }
}

public class Trouble {
  public static void main(String[] args) {
    System.out.println(new B());
  }
}