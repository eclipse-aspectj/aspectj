package dk.infimum.aspectjtest;
public class Main2 {
 
    public static void main(String[] args) {
      Main2 obj = new Main2();
      // swap following lines to change behavior
      obj.test(null, new Main2[]{});
      obj.test(null, null);
    }
 
    void test(Main2 dummy, Main2[] dummy2) {}
 
}
