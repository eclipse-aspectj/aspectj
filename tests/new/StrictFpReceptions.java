import org.aspectj.testing.*;

public class StrictFpReceptions {
	// reception counter
	static int r_counter = 0;
	// call counter
	static int c_counter = 0;

    public static void main(String[] args) {
        StrictClass s = new StrictClass();
        StrictClassAbstract sa = s;

        cleanup();
        s.test1();
        Tester.check
            (r_counter==1 && c_counter==1,
             "test1 method call, " +
             "counters="+r_counter+","+c_counter);

        cleanup();
        sa.test2();
        Tester.check
            (r_counter==0 && c_counter==0,
             "test2 method call, " +
             "counters="+r_counter+","+c_counter);

        cleanup();
        sa.test3();
        Tester.check
            (r_counter==1 && c_counter==1,
             "test3 method call, " +
             "counters="+r_counter+","+c_counter);

        cleanup();
        sa.test4();
        Tester.check
            (r_counter==1 && c_counter==1,
             "test4 static method call, " +
             "counters="+r_counter+","+c_counter);

        cleanup();
        sa.test5();
        Tester.check
            (r_counter==0 && c_counter==0,
             "test5 static method call, " +
             "counters="+r_counter+","+c_counter);

    }

    private static void cleanup() {
       	r_counter = c_counter = 0;
    }

}

aspect StrictFpWatcher {
	pointcut r_strict() : execution(strictfp * *(..));
	pointcut c_strict() : call(strictfp * *.*(..));

        before() : r_strict() { StrictFpReceptions.r_counter++; }
        before() : c_strict() { StrictFpReceptions.c_counter++; }
}


abstract class StrictClassAbstract {
	float f;
	double d;
	StrictClassAbstract() {}
	StrictClassAbstract(double _d) { d = _d; }
	public abstract float test1();
	public float test2() { return 0.f; }
	public strictfp float test3() { return 0.f; }
	public static strictfp float test4() { return 0.f; }
	public static float test5() { return 0.f; }

};

strictfp class StrictClass extends StrictClassAbstract {
	public float test1() { return 0.f; }

}

