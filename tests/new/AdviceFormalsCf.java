import org.aspectj.testing.Tester;

/**
 * PR#544 the formals passed to advice should behave just like
 *        any other method formal
 */

public class AdviceFormalsCf {
    public static void main(String[] args) {
        Tester.checkEqual(new C().m("bye"), "foo");
        Tester.checkEqual(new C().mi(2.), 1);
    }
}

class C {
    public String m(Object p) {
        return "foo";
    }

    public int mi(double p) {
        return 1;
    }
}


aspect A {
    after(final Object p) returning(final Object o): call(* C.m*(*)) && args(p) {
        p = Boolean.TRUE; //ERR: p is final
        o = Boolean.TRUE; //ERR: o is final
        Tester.checkEqual(p, Boolean.TRUE);
        Tester.checkEqual(o, Boolean.TRUE);
    }

    Object around(final Object p, final Object o): call(* C.m*(*)) && args(p) && target(o) {
        Object ret = proceed(p, o);
        p = Boolean.TRUE; //ERR: p is final
        o = Boolean.TRUE; //ERR: o is final
        Tester.checkEqual(p, Boolean.TRUE);
        Tester.checkEqual(o, Boolean.TRUE);
        return ret;
    }
        

}
