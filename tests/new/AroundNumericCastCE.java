
import org.aspectj.testing.Tester;

/** @testcase PR#838 checking around join point for advice return type - numeric */
public class AroundNumericCastCE {
    
    public static double doubleMethod(int total){
        return (double) total;
    }
    
    public static int intMethod(int total){
        return total;
    }
    
    public static aspect Selector{
        // expect CE "illegal return value" from execution(int intMethod(int))
        double around(int i) : execution(* *Method(int)) && args(i) { // CE 17
            return proceed(i);
        }
    }
    public static void main(String[] args){
        double result = doubleMethod(1000);
        Tester.check(result != 0, "calculatePayment(1000)");
    }
}
    
