// incorrect signature
public class Three {
  public static void main(String []argv) {
    Integer[] Is = new Integer[5];
  }
}

aspect X {
  before(): execution(Integer.new(..)) { } // no match, it's execution
  before(): call(Integer.new(..)) { } // no match, it's an integer array
  before(): execution(Integer[].new(..)) { } // no match, no execution jp 
  before(): call(Integer[].new()) { } // no match, the call takes an int
}
