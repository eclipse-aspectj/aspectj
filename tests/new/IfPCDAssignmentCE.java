
public class IfPCDAssignmentCE {
    public static void main (String[] args) {
        foo(new C());
    } 
    static void foo(C a) {}
}

class C {
    static boolean doit() { return true; }
    static boolean doit(C a) { return true; }
}

aspect A {
    before(C c) : args(c) && call(void foo(C)) 
        && if ((c=new C()).doit()) { // CE 16
    }

    before(C c) : args(c) && call(void foo(C)) 
        && if (C.doit(c = new C())) {  // CE 20
    }
}
/*
  Expecting compiler error on attempts to assign bound variable
  in if() PCD.  Currently getting compiler stack trace.
 */
