public class CflowBinding {
    public static void main(String[] args) {
    }

    public void m1() {}
    public void m2() { m1(); }
}

aspect A {
    public static Object foo = null;

    before(final Object t): execution(void m1()) && this(t) && 
        cflow(execution(void m2()) && if(t == foo))    //CE
    {
    }
}
