import org.aspectj.testing.Tester;
public class ScopesAndFields_PR191 {
    public static void main(String[] args) {
        new ScopesAndFields_PR191().realMain(args);
    }
    public void realMain(String[] args) {
        C c = new C();
        c.a();
        Tester.checkEqual(c.c(), "C");
        Tester.checkEqual(c.c,   "c");
        Tester.checkEqual(c.t(), "c");
    }
}

class C {
    public String c = "c";
    public String c() {
        Object c = "C";
        return c+"";
    }
    public String t() {
        Object c = "C";
        return this.c;
    }
    public void a() {
        String c = "C";
        Tester.checkEqual(c+"", "C");
        Tester.checkEqual(this.c, "c");
    }
}
