import org.aspectj.testing.Tester;

/**
 * @testTarget compilerErrors.false.exceptions Throwable treated as checked, false compiler error
 */
public class FalseThrowsCE {
    public static void main(String[] args) {
		try {
			System.getProperty("").toString(); // potential NPE, SecurityException
		} catch (Throwable e) {    // (false) CE: Throwable never thrown
			String s = "got " + e;  
		}
		Tester.check(true,"ok - compiled without error");
    }
}
