
import org.aspectj.testing.Tester;
import java.util.*;

/** @testcase PR#909 instance as class reference to constant */
public class InstanceAsClassRefToConstant {
	public static void main(String[] args) {
		throw new Error("XXX not set up to run");
	}
}

abstract class CWithLongConst {
	public static final long MAX = 1000000;
} 

class A extends CWithLongConst {
}

class TestCase {
  public final static void main(String[] argv) {
    A aL = new A();

    // bad error
    // a) Sanity check:
    //      stack size is -1 after stmt BreakStmt(label: null) (warning)
    for (long l=0; l<2000000; l+=100000) {
      if (l > aL.MAX) {
        break;
      } 
    }

    // b) Sanity check: stack size is -1 after stmt ExprStmt() (warning)
    String[] stringsL = null;
    for (long k=0; (k<2000000) && (stringsL == null); k+=100000) {
      if (k > aL.MAX) {
        stringsL = new String[1];
      }
    }
  }
}
