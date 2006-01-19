public class Eight {
  public static void main(String []argv) {
    Integer[][] Is = new Integer[5][6];
    int[][] is = new int[2][4];
  }
}

aspect X {
  before(): call(int[][].new(int,int)) { System.err.println("advice running 1");}
  before(): call(Integer[][].new(int,int)) { System.err.println("advice running 2");}
}
