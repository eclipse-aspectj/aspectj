public class AspectInheritance2 {
    public static void main(String[] args) {
    }
}


abstract aspect Base {
    abstract pointcut targets(int i, C c);

    after(int i, C c): targets(i, c) {
	//
    }
}

aspect FullConcrete extends Base {
    pointcut targets(int i, SubC subc):      //ERROR param types must match exactly
        call(void SubC.m(double)) && target(subc) && args(i);
}

aspect ForgetfulConcrete extends Base { //ERROR must concretize abstracts
}

aspect ExplictAbstractConcrete extends Base {
    pointcut targets(int i, C c);

    abstract pointcut newTargets();  //ERROR no abstracts allowed in concrete
}

aspect PrivateConcrete extends Base {
    private pointcut targets(int i, C c):  //ERROR can't reduce visibility of decs
        call(void C.m(int)) && target(c) && args(i);
}


class C {
    public void m(int i) { }
}

class SubC extends C {}
