import org.aspectj.testing.Tester;

public class Binkley {
    public static void main(String[] args) {
        new Binkley().realMain(args);
    }
    public void realMain(String[] args) {
        Tester.check(true, "compiled!");
    }
}

aspect A percflow(pc())
{
    pointcut pc() : within(Binkley);
    before(): cflow(pc()) && !(call (new())) && !within(A) { }
}
