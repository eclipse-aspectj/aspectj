import org.aspectj.testing.*;
import java.util.*;

public class PR353c {

   public static void main(String[] args){
     new PR353c().go();
   }
   
   void go(){
       C c = new C(); c.f(); c.g();
       A a = new A(); a.f(); a.g();
       B b = new B(); b.f(); b.g();
       D d = new D(); d.f(); d.g();
       E e = new E(); e.f(); e.g();
   }
}

interface I { }

class C {
    void f() {}
    void g() {}
}

class A extends C {
    void f() {}
}

class B extends C {
    void f() {}
    void g() {}
}

class D extends C { }
class E extends C implements I { }

aspect AA extends AspectSupport {

    pointcut f(): receptions(void f());
    pointcut g(): receptions(void g());
    pointcut b(): f() || g();

    pointcut all(): b();
    pointcut notC(): b() && !instanceof(C);
    pointcut notD1(): b() && instanceof(C) && !instanceof(D);
    pointcut notD2(): b() && !instanceof(D) && instanceof(C);
    pointcut notI(): b() && !instanceof(I);
    pointcut notA1(): b() && instanceof(C) && !instanceof(A);
    pointcut notA2(): b() && !instanceof(A) && instanceof(C);
    pointcut notB1(): b() && instanceof(C) && !instanceof(B);
    pointcut notB2(): b() && !instanceof(B) && instanceof(C);
    pointcut notE1(): b() && instanceof(C) && !instanceof(E);
    pointcut notE2(): b() && !instanceof(E) && instanceof(C);



    static before(): all() { p("cabde", thisJoinPoint.className); }
    static before(): notC() { p("", thisJoinPoint.className); }
    static before(): notD1() { p("cabe", thisJoinPoint.className); }
    static before(): notD2() { p("cabe", thisJoinPoint.className); }
    static before(): notI() { p("cabd", thisJoinPoint.className); }
    static before(): notA1() { p("cbde", thisJoinPoint.className); }
    static before(): notA2() { p("cbde", thisJoinPoint.className); }
    static before(): notB1() { p("cade", thisJoinPoint.className); }
    static before(): notB2() { p("cade", thisJoinPoint.className); }
    static before(): notE1() { p("cadb", thisJoinPoint.className); }
    static before(): notE2() { p("cadb", thisJoinPoint.className); }

    pointcut _b():  receptions(* *());
    
    pointcut _all(): _b();
    pointcut _notC(): _b() && !instanceof(C);
    pointcut _notD1(): _b() && instanceof(C) && !instanceof(D);
    pointcut _notD2(): _b() && !instanceof(D) && instanceof(C);
    pointcut _notI(): _b() && !instanceof(I);
    pointcut _notA1(): _b() && instanceof(C) && !instanceof(A);
    pointcut _notA2(): _b() && !instanceof(A) && instanceof(C);
    pointcut _notB1(): _b() && instanceof(C) && !instanceof(B);
    pointcut _notB2(): _b() && !instanceof(B) && instanceof(C);
    pointcut _notE1(): _b() && instanceof(C) && !instanceof(E);
    pointcut _notE2(): _b() && !instanceof(E) && instanceof(C);   

    static before(): _all() { p("cabde", thisJoinPoint.className); }
    static before(): _notC() { p("", thisJoinPoint.className); }
    static before(): _notD1() { p("cabe", thisJoinPoint.className); }
    static before(): _notD2() { p("cabe", thisJoinPoint.className); }
    static before(): _notI() { p("cabd", thisJoinPoint.className); }
    static before(): _notA1() { p("cbde", thisJoinPoint.className); }
    static before(): _notA2() { p("cbde", thisJoinPoint.className); }
    static before(): _notB1() { p("cade", thisJoinPoint.className); }
    static before(): _notB2() { p("cade", thisJoinPoint.className); }
    static before(): _notE1() { p("cadb", thisJoinPoint.className); }
    static before(): _notE2() { p("cadb", thisJoinPoint.className); }    
}

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
aspect AspectSupport {

    public static Map map = new HashMap();
    static {
        String[] ss = {

        };
        for (int i = 0; i < ss.length; i++) {
            map.put(ss[i], new Vector());
        }
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


