public class Seven {
  public static void main(String []argv) {
    int[] is = new int[5];
  }
}

aspect X {
  before(): call(int[].new(int)) { System.err.println("advice running");}
}
