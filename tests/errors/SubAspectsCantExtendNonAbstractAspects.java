public class SubAspectsCantExtendNonAbstractAspects {
    public static void main(String[] args) {
    }
}

class C {}

aspect A /*of eachobject(instanceof(C))*/ {
    before(): call(* *(..)) {}
}


//ERROR: can't extend a concrete aspect
aspect SubA extends A {
}
