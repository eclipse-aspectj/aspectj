import org.aspectj.testing.*;
import java.io.*;

public class PR355 {
    public static void main(String[] args) {
        new PR355().go();
    }

    static {
        String[] types  = { "static", "non", "instance" };
        String[] advice = { "before", "after", "around" };
        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < advice.length; j++) {
                Tester.expectEvent(types[i] + "-" + advice[j]);
            }
        }
        Tester.expectEventsInString("C.f,C.e");
    }

    void go() {
        new C().f();
        Tester.checkAllEvents();
    }
}


class C {
    void f() { Tester.event("C.f"); e(); }
    void e() { Tester.event("C.e"); }
}

abstract aspect Cuts {
    pointcut p(): within(C) && call(* C.*(..));
    static void a(String s) { Tester.event(s); }
}

/* Static aspects have no problem */
aspect StaticAspect extends Cuts {
    before():      p() { a("static-before"); }
    void around(): p() { a("static-around"); proceed();  }
    after ():      p() { a("static-after");  }
}

/* Non-static aspects have a problem */
aspect NonStaticAspect extends Cuts issingleton() {
    before():      p() { a("non-before"); }
    void around(): p() { a("non-around"); proceed();  }
    after ():      p() { a("non-after");  }
}

/* No problem here */
aspect InstanceOfAspect extends Cuts perthis(this(C)) { 
    before():      p() { a("instance-before"); }
    void around(): p() { a("instance-around"); proceed(); }
    after ():      p() { a("instance-after");  }
}
