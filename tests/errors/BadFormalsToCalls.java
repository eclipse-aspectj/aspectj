import org.aspectj.testing.Tester;
public class BadFormalsToCalls {

    static boolean noargsCalled = false;
    
    public static void main(String[] args) {
        new BadFormalsToCalls().go();
    }

    void go() {
        new B().noargs();
        Tester.check(noargsCalled, "noargs wasn't called");
    }
    
    class B {
        public void noargs() {
        }
    }    
}

aspect CallsNoArgsAspect {
    pointcut noargs(BadFormalsToCalls.B b): call(void noargs());
    void around(BadFormalsToCalls.B b): noargs(b) {
        proceed(b);
    }
}
