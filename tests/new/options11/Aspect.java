

import org.aspectj.testing.Tester;
import org.aspectj.lang.JoinPoint;

import library1.Library1;
import library2.Library2;

/** extend abstract, and implement needed */
aspect AnotherAspect extends Library2 {

    public pointcut targetJoinPoints() : 
        execution(public static void Main.main(..));

    protected String renderId(JoinPoint jp) {
        String result = super.renderId(jp);
        return result + " - ok";
    }
}

class Testing {
    static aspect Init {
        
        declare precedence : Init, Library1, AnotherAspect;
        
        before() : AnotherAspect.targetJoinPoints() {
            Main.i = 1;
            Tester.expectEvent("before main");
            Tester.expectEvent("after main");
            Tester.expectEvent("after 2 main - ok");
            Tester.expectEvent("before 2 main - ok");
            Tester.expectEvent("before run");            
        }
        after () returning : AnotherAspect.targetJoinPoints() {
            Tester.checkAllEvents();
        }
        
        before() : call(void run()) {
            Tester.event("before run");
        }
    }

}