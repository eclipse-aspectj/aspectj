import org.aspectj.testing.Tester;

public class Cricket {
    public static void main(String[] args) {
        Lib l = new Lib();
        Tester.event("call stringMethod");
        l.stringMethod(2);
        Tester.event("call voidMethod");
        l.voidMethod(2);
        Tester.checkEventsFromFile("Cricket.out");
    }
}


class Lib {
    public void voidMethod(int count) {
        if (count == 0) return;
        else voidMethod(count - 1);
    }

    public String stringMethod(int count) {
        if (count == 0) return "0";
        else return count + "-" + stringMethod(count-1);
    }
}


aspect Trace {
    pointcut entry(): target(Lib) && call(* *(..));
    pointcut topEntry(): entry() && !cflowbelow(entry());

    before(): topEntry() {
        Tester.event("->top entry: " + thisJoinPoint);
    }

    after(): entry() {
        Tester.event("->exit: " + thisJoinPoint);
    }

    after() returning (Object o): entry() {
        Tester.event("->exit: " + thisJoinPoint + " with " + o);
    }
    after(): topEntry() {
        Tester.event("->top exit: " + thisJoinPoint);
    }

    after() returning (Object o): topEntry() {
        Tester.event("->top exit: " + thisJoinPoint + " with " + o);
    }
}
