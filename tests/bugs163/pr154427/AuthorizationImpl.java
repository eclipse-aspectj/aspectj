public class AuthorizationImpl implements AuthorizationAdmin {

      /* ========== interface Authorization ============*/

      public boolean mayPerform(String user, String action) {
            System.out.println("mayPerform() executing");
            return true;
      }

      /* ========== interface AuthorizationAdmin  ============*/

      public boolean mayPerform2(String user, String action) {
            System.out.println("mayPerform2() executing");
            return true;
      }

}

