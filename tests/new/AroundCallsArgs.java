import org.aspectj.testing.Tester;

public class AroundCallsArgs {
    public static void main(String[] args) {
	new CL().go();
	//Tester.checkEqual(A.data, "CL:CP(hello)");
        Tester.checkEqual(A.data, "CP(hello)");
    }
}

aspect A /*of eachobject(instanceof(CL))*/ {
    public static String data = "";

    //pointcut parseCalls(CL cl, CP cp, String cmd):
    pointcut parseCalls(CP cp, String cmd):
        //calls(void cp1.parse(cmd1)) && within(cl1);
        (target(cp) && args(cmd) && call(void CP.parse(String))) &&
        within(CL);

    //void around(CL cl, CP cp, String cmd):
    void around(CP cp, String cmd):
        //parseCalls(cl, cp, cmd) {
        parseCalls(cp, cmd) {
	//data = cl.getClass().getName()+":"+cp.getClass().getName()+"("+cmd+")";
        data = cp.getClass().getName()+"("+cmd+")";
	//proceed(cl, cp, cmd);
        proceed(cp, cmd);
    }
}

class CL {
  void go() {
    new CP().parse("hello");
  }
}

class CP {
  void parse(String cmd) {
  }
}
