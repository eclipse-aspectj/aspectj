import org.aspectj.testing.Tester;

public class MethodsWithTheSameName {
    public static void main(String[] args) {
        Inner i = new Inner("inner");
        i.f((String)null);
        i.f("call1");
        i.f(new Inner("call2"));
        Tester.checkEqual(strings, "null:inner-null:null-inner:null:call2:");
    }

    static String strings = "";

    static class Inner {
        String s;
        Inner(String s) { this.s = s; }
        void f(String str) {
            f(str == null ? null : new Inner("null-"+s));
            f(str == null ? new Inner(s+"-null") : null);
        }
        void f(Inner i) { strings += i + ":"; }
        public String toString() { return s; }
    }
}


