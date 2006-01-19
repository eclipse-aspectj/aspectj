// basics
public class One {
  public static void main(String []argv) {
    Integer[] is = new Integer[5];
  }
}

aspect X {
  before(): call(Integer[].new(..)) {
	  System.err.println("advice running");
  }
}
