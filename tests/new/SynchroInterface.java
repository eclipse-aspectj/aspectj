import org.aspectj.testing.Tester;
import java.lang.reflect.*;

public class SynchroInterface {
    public static void main(String[] args) {
        try {
            new SynchroInterface().realMain(args);
        } catch (Throwable t) {
            Tester.check(false, "uh oh " + t);
        } finally {
            Tester.check(Consts.ran, "method didn't run");
        }
    }
    public void realMain(String[] args) throws Throwable {
        Class.forName("EmptyClass").getMethod("method", new Class[]{}).invoke(new EmptyClass(), new Class[]{});
    }
}

class Consts {
    public static boolean ran = false;
}

class EmptyClass {
}

interface EmptyInterface {
}

aspect IntroType {
    introduction EmptyClass {
        implements EmptyInterface;
    }
}

aspect IntroMethod {
    introduction EmptyInterface {
        public synchronized void method() {
            Consts.ran = true;
        }
    }
}

