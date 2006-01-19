// accessing the size of the array
public class Six {
  public static void main(String []argv) {
    Integer[] Is = new Integer[5];
  }
}

aspect X {
  before(int n): call(Integer[].new(int)) && args(n) { System.err.println("Array size = "+n);}
}