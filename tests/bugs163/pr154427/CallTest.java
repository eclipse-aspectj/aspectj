public class CallTest {


 public static void main(String[]argv) {
   new CallTest().testMayPerform();
   new CallTest().testMayPerform2();
   new CallTest().testMayPerform3();
 }

      private AuthorizationAdmin admin;
      private Authorization auth;

      public void testMayPerform() {
        admin = new AuthorizationImpl();
        boolean bool = admin.mayPerform("peter", "query");
        if (!bool) throw new RuntimeException();
      }

      public void testMayPerform2() {
            admin = new AuthorizationImpl();

            boolean bool = admin.mayPerform2("peter2", "query2");
   if (!bool) throw new RuntimeException();

      }

        public void testMayPerform3() {
                auth = new AuthorizationImpl();

                boolean bool = auth.mayPerform("peter2", "query2");

   if (!bool) throw new RuntimeException();
        }

}

