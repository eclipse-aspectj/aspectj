
import pClass.Class;
import pAspect.Aspect;
import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }
    
    public static void test() {
	Class     f  = new Class();
	f.foo();
	Tester.check(Aspect.ranAdvice, "advice on class in different package");
    }
}
