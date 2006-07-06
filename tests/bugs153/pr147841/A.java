package pkg;

public aspect A {

        before() : execution(* pack.C.method1()) && this(pack.C) {
                System.err.println("before exec method1 and this is C");
        }

        before() : call(* pack.C.method2()) && target(pack.C) {
                System.err.println("before call to method2 and target is C");
        }
}
