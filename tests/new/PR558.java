/** @testcase PR#558 class name for identifier as String should provoke error */
public class PR558 {
  public static void main( String args[] ) {
      new StringBuffer().append(PR558); // expecting compiler error that PR558 is a bad symbol
  }
}

