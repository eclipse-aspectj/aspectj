public class TestLib {

          public static void main(String[] args) {
                System.err.println("obtaining five, got "+new TestLib().getFive());
          }
          
          public int getFive() { return 5; }
}