
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 
import java.rmi.RMISecurityException; // deprecated class

/** @testcase PR#602 PUREJAVA no deprecation warnings (regardless of -deprecation flag) */
public class DeprecationWarning {
    public static void main(String[] args) {
        boolean result = false;
        try {
            if (!result) throw new RMISecurityException("ok"); // CW 11 deprecated class
        } catch (RMISecurityException e) {
            result = true;
        }
        Tester.check(result, "no RMISecurityException");
    }
    
}
