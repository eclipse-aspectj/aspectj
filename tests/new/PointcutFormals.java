import org.aspectj.testing.*;

public class PointcutFormals  {
    public static void main(String[] args) {
        new PointcutFormals().call(0);
        Tester.checkAllEvents();
    }
    void call(int i) {}

    static {
        String[] cuts = { "calls_pc", "receptions_pc", "executions_pc" };
        String[] kinds = { "before", "after", "around" };
        for (int i = 0; i < cuts.length; i++) {
            for (int j = 0; j < kinds.length; j++) {
                Tester.expectEvent(kinds[j] + "." + cuts[i]);
            }
        }
    }
}

aspect Aspect {
    pointcut calls_pc     (): call(void *.call(int)) && within(PointcutFormals);
    pointcut receptions_pc(): call(void PointcutFormals.call(int));
    pointcut executions_pc(): execution(void *(int));

    before(): calls_pc     () { a("before.calls_pc");      }
    before(): receptions_pc() { a("before.receptions_pc"); }
    before(): executions_pc() { a("before.executions_pc"); }

    after(): calls_pc     () { a("after.calls_pc");      }
    after(): receptions_pc() { a("after.receptions_pc"); }
    after(): executions_pc() { a("after.executions_pc"); }

    around() returns void: calls_pc     () { a("around.calls_pc");      proceed(); }
    around() returns void: receptions_pc() { a("around.receptions_pc"); proceed(); }
    around() returns void: executions_pc() { a("around.executions_pc"); proceed(); }

    void a(Object msg) {
        Tester.event(msg);
    }
}
