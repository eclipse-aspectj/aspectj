public class QualifiedNewCP {
    public static void main(String[] args) {
        Base b = new Base();
        I o = b.new Inner();
        o.m();
        o = b.new AbstractInner() { public void m() { System.out.println("mi"); helper(); } };
        o.m();
        o = b.new Inner() { public void m() { System.out.println("mi"); } };
        o.m();
    }
}

class Base {
    class Inner implements I {
        public void m() { System.out.println("m"); }
    }
    abstract class AbstractInner implements I {
        //public abstract void m();
        protected void helper() { System.out.println("helper"); }
    }

    protected void foo() {
        System.out.println("foo");
    }
}

interface I {
    public void m();
}
