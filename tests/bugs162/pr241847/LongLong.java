public class LongLong implements II {
   public void m1(String arg) {
   }

   public static void main(String [] args) throws Exception {
      try {
         new LongLong().m1(null);
         throw new Exception("(BAD) advice did not run");
      } catch (RuntimeException e) {
         System.out.println("(GOOD) advice ran and threw expected exception");
         e.printStackTrace(System.out);
      }
   }
}
