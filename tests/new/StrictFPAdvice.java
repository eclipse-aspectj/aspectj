import org.aspectj.testing.Tester;

public class StrictFPAdvice {
    public static void main(String[] args) {
	m(2.0);
    }

    static double m(double a) { return a; }
}

aspect A {
    pointcut points(double d): call(double StrictFPAdvice.m(double)) && args(d);

    strictfp before(double d): points(d) {
	//XXX insert a test here that this body really is strictfp
    }

    strictfp double around(double d): points(d) {
	//XXX insert a test here that this body really is strictfp
	return proceed(d);
    }
}
