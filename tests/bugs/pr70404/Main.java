package dk.infimum.aspectjtest;
public class Main {
 
    public static void main(String[] args) {
      Main obj = new Main();
      //Main m[] = new Main[3];
      
      // swap following lines to change behavior
      obj.test(null, null);
      obj.test(null, new Main[]{});
    }
 
    void test(Main dummy, Main[] dummy2) {}
 
}
