public class AspectInheritance3 {
    public static void main(String[] args) {
    }
}


abstract aspect Base {
    abstract pointcut targets(int i, C c);

    after(int i, C c): targets(i, c) {
	//
    }
}
aspect GoodConcrete extends Base {
    pointcut targets(int i, C c);
}

// this aspect is illegal because concrete-concrete extension is illegal
aspect DoubleConcrete extends GoodConcrete {
}

aspect OtherAspect {
    // can't reference an abstract pointcut using a static reference
    before(): Base.targets(int, C) { }
}


class C {
    public void m(int i) { }
}

class SubC extends C {}
