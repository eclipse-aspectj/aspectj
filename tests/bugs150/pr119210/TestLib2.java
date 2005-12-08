public class TestLib2 {

          public static void main(String[] args) {
                System.err.println("obtaining five, got "+new TestLib2().getFive());
          }
          
          public Integer getFive() { return new Integer(5); }
}