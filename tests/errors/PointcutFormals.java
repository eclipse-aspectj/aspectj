import org.aspectj.testing.Tester;

public class PointcutFormals {
    public static void main(String[] args) {
        new PointcutFormals().call(0);
        Tester.check(false, "Shouldn't have compiled!");
    }
    void call(int i) {}
}

aspect Aspect {
    int n;

    pointcut calls_pc1     (int n): call(void *.call(n));
    pointcut calls_pc2     (int n): call(void *.call(..));
    pointcut calls_pc     (): call(void *.call(n));
    pointcut executions_pc(): execution(void *(n));

    before(): calls_pc     () { }
    before(): executions_pc() { }
    
    after(): calls_pc     () { }
    after(): executions_pc() { }
    
    void around(): calls_pc     () { }
    void around(): executions_pc() { }
}
