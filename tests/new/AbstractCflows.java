import org.aspectj.testing.Tester;

public class AbstractCflows {
    public static void main(String[] args) {
        C c = new C();
        c.enter(1, 2);
        c.enter1(3);
        Tester.checkEvents(expected);
    }
    private static final String[] expected = {
        "enter(1, 2)",
        "CflowX: call(void C.body()); i = 1",
        "CflowY: call(void C.body()); i = 2",
        "PerCflowX: call(void C.body())",
        "PerCflowY: call(void C.body())",
        "BODY",
        "CflowX: call(void C.body()); i = 1",
        "CflowY: call(void C.body()); i = 2",
        "PerCflowX: call(void C.body())",
        "PerCflowY: call(void C.body())",
        "BODY",
        "enter1(3)",
        "Cflow3: call(void C.body()); i = 3",
        "PerCflow3: call(void C.body())",
        "BODY",
    };
}

class C {
    public void enter(int x, int y) {
        Tester.event("enter(" + x + ", " + y + ")");
        body();
        body();
    }

    public void enter1(int i) {
        Tester.event("enter1(" + i + ")");
        body();
    }

    public void body() {
        Tester.event("BODY");
    }
}

abstract aspect CflowBase {
    abstract pointcut entry(int i);
    pointcut flow(int x): cflow(entry(x));

    pointcut body(): call(* body());

    before(int i): body() && flow(i) {
        Tester.event(this.getClass().getName() + ": " + thisJoinPoint + "; i = " + i);
    }
}

aspect CflowY extends CflowBase {
    pointcut entry(int y): args(*, y) && call(void enter(int, int));
}

aspect CflowX extends CflowBase {
    pointcut entry(int x): args(x, *) && call(void enter(int, int));
}

aspect Cflow3 extends CflowBase {
    pointcut entry(int i): args(i) && call(void enter1(int));
}

abstract aspect PerCflowBase percflow(entry(int)) {
    abstract pointcut entry(int i);

    pointcut body(): call(* body());

    before(): body() {
        Tester.event(this.getClass().getName() + ": " + thisJoinPoint);
    }
}

aspect PerCflowY extends PerCflowBase {
    pointcut entry(int y): args(*, y) && call(void enter(int, int));
}

aspect PerCflowX extends PerCflowBase {
    pointcut entry(int x): args(x, *) && call(void enter(int, int));
}

aspect PerCflow3 extends PerCflowBase {
    pointcut entry(int i): args(i) && call(void enter1(int));
}

