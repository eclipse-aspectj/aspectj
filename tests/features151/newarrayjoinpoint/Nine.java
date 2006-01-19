public class Nine {
  public static void main(String []argv) {
    Integer[][] Is = new Integer[5][6];
    int[][] is = new int[2][4];
  }
}

aspect X {
  before(int a,int b): call(int[][].new(int,int)) && args(a,b) { System.err.println("advice running 1 ("+a+","+b+")");}
  before(int a,int b): call(Integer[][].new(int,int)) && args(a,b) { System.err.println("advice running 2 ("+a+","+b+")");}
}
