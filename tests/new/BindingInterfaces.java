import org.aspectj.testing.Tester;
import java.util.*;
public class BindingInterfaces {
    public static void main(String[] args) {
        new BindingInterfaces().realMain(args);
    }
    public void realMain(String[] args) {

        I i0 = new I(){}; Tester.checkEqual(str(i0), "I", "i0");
        I ij = new J(){}; Tester.checkEqual(str(ij), "JI", "ij");
        I ik = new K(){}; Tester.checkEqual(str(ik), "KJI", "ik");

        J j0 = new J(){}; Tester.checkEqual(str(j0), "JI", "j0");
        J jk = new K(){}; Tester.checkEqual(str(jk), "KJI", "jk");

        K k0 = new K(){}; Tester.checkEqual(str(k0), "KJI", "k0");
    }

    private String str(Object o) {
        return str(o.getClass().getInterfaces()[0]);
    }

    private String str(Class c) {
        String str = c.getName();
        Class[] is = c.getInterfaces();
        for (int i = 0; i < is.length; i++) {
            str += str(is[i]);
        }
        return str;
    }
}

interface I {}
interface J {} //extends I {}
interface K {} //extends J {}

aspect Aspect {
    declare parents: J implements I;
    declare parents: K implements J;
}
