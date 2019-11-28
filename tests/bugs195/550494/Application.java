package foo;

import java.sql.SQLException;

public class Application {
  public static void main(String[] args) throws SQLException {
    System.out.println("Aspect should not kick in without ITD, but should with ITD");
    new BaseClass().getConnection();
    new BaseClass().getConnection("user", "pw");

    System.out.println("Aspect should kick in");
    new SubClass().getConnection();
    new SubClass().getConnection("user", "pw");
  }
}
