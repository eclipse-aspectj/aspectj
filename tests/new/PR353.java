import org.aspectj.testing.*;
import java.util.*;

public class PR353 {
    public static void main(String[] args) {
        new PR353().go(args);
        Tester.checkAllEvents();
    }

    void go(String[] args) {
        A a = new A();
        B b = new B();
        C c = new C();
        D d = new D();
        E e = new E();
        a.f();
        b.f();
        c.f();
        d.f();
        e.f();
        new Verifier().verify(Aspect.map);
    }
}

interface I             { public void f();    }
class A                 { public void f() { } }
class B implements I    { public void f() { } }
class C                 { public void f() { } }
class D extends C       { public void f() { } }
class E extends B       { public void f() { } }
class F extends C
    implements I        { public void f() { } }

class Verifier {
    void verify(Map map) {
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = (iter.next() + "").toLowerCase();
            List list = (List) map.get(key);
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Object onext = it.next();
                String next = (onext + "").toLowerCase();
                if (key.indexOf(next) == -1) {
                    Tester.check(false, next + " not found in " + key);
                } else {
                    it.remove();
                }
            }
            Tester.check(list.size() == 0, list + " contains classes excluded");
        }
    }
}
aspect Aspect {

    pointcut r(): receptions(* f());
    pointcut abcdef(): r();
    pointcut acd():   r() && !instanceof(I);
    pointcut acdf():   r() && !instanceof(B);
    pointcut a():     r() && !instanceof(B) && !instanceof(C);

    public static Map map = new HashMap();
    static {
        String[] ss = {
            "abcdef",
            "acd",
            "acdf",
            "a",
        };
        for (int i = 0; i < ss.length; i++) {
            map.put(ss[i], new Vector());
        }
    }

    static before(): abcdef() {
        p("abcdef", thisJoinPoint.className);
    }

    static before(): acd() {
        p("acd", thisJoinPoint.className);
    }

    static before(): acdf() {
        p("acdf", thisJoinPoint.className);
    }

    static before(): a() {
        p("a", thisJoinPoint.className);
    }

    static void p(String key, String str) {
        List list = (List) map.get(key);
        if (list == null) {
            list = new Vector();
        }
        list.add(str);
        map.put(key, list);
    }

    static List v(Object[] os) {
        List v = new Vector();
        for (int i = 0; i < os.length; i++) {
            v.add(os[i]);
        }
        return v;
    }
}
