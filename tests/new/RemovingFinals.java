import org.aspectj.testing.Tester;
import java.lang.reflect.*;
public class RemovingFinals {
    public static void main(String[] args) {
        new RemovingFinals().realMain(args);
    }
    public void realMain(String[] args) {
        try {
            Tester.check((C.class.getField("public_i").getModifiers()
                          & Modifier.FINAL) != 0, "public_i is not final");
        } catch (Throwable t) {
            Tester.throwable(t);
        }
    }
}

class C {
    public final int public_i = 1;
    static int x = 2;

    private final int CONST = 0;
    public void m() {
        switch(x) {
        case (CONST): System.out.println("no");
        }
    }
}

// make things a little difficult
aspect A {
    before(): staticinitialization(C) { new StringBuffer().append(thisJoinPoint); }
    before(): execution(C.new(..)) { new StringBuffer().append(thisJoinPoint); }
}
