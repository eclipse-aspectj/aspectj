import org.aspectj.testing.Tester;

public class AroundAndCalls {
    public static void main(String[] args) {
	Tester.checkEqual(new CL().go(), "basic-advised");
    }
}

aspect MustAspect /*of eachobject(instanceof(CL))*/ {
    
    pointcut parseCalls(CP cp, String cmd):
        (args(cmd) && target(cp) && call(String CP.parse(String))) &&
        within(CL);
    
    String around(CP cp, String cmd): parseCalls(cp, cmd) {
        return proceed(cp, cmd + "-advised");
    }
}

class CL {
    String go() {
	return new CP().parse("basic");
    }
}

class CP {
    String parse(String cmd) {
	return cmd;
    }
}
