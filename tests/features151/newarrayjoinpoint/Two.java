// Using wildcard for type
public class Two {
  public static void main(String []argv) {
    Integer[] Is = new Integer[5];
  }
}

aspect X {
  before(): call(*.new(..)) {
	  System.err.println("advice running");
  }
}
