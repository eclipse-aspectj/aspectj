public class AspectInheritance1 {
    public static void main(String[] args) {
    }
}


abstract aspect Base {
    abstract pointcut targets(int i, C c);

    after(int i, C c): targets(i, c) {
	//
    }
}

aspect EmptyConcrete extends Base {
    // this would match everything, but we declare it a syntax error
    pointcut targets(int i, C c): ;
}

class C {}
