public class NoAspect {
    public static void main(String[] args) {
        new NoAspect().go();
    }

    void go() {
    }
}

class A {
    static pointcut p(): target(*) && call(* go(..));
    before(): p() {
    }
}
