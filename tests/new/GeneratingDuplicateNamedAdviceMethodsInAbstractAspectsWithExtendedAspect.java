import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class
    GeneratingDuplicateNamedAdviceMethodsInAbstractAspectsWithExtendedAspect
{
    public static void main(String[] args) {
        new GeneratingDuplicateNamedAdviceMethodsInAbstractAspectsWithExtendedAspect().realMain(args);
    }
    public void realMain(String[] args) {
        new C().c();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("c");
        String[] strs = {"before","after","around"};
        for (int i = 0; i < strs.length; i++) {
            int[] is = new int[] {1,2};
            for (int j = 0; j < is.length; j++) {
                Tester.expectEvent(strs[i]+is[j]);
            }
        }
        
    }
}

class C {
    public void c() { Tester.event("c"); }
}

abstract aspect A {
    pointcut c(): call(void C.c());
    protected static void a(String str, Object o) {
        Class c = o.getClass();
        Tester.event(str);
        Tester.check(!A.class.equals(c), "A is abstract!");
        Tester.check(Inner.class.equals(c), "Inner must equal "+c);
    }
    after():       c() { a("after1", this);  }
    after():       c() { a("after2", this);  }
    before():      c() { a("before1", this); }
    before():      c() { a("before2", this); }
    void around(): c() { a("around1", this); proceed(); }
    void around(): c() { a("around2", this); proceed(); }
    static aspect Inner extends A {}
}
