public class New {
    public static void main(String[] args) {
        new New().go();
    }

    void go() {

    }
}

aspect A {
    pointcut p(): call(* *.new(..)) && this(*);
    before(): p() {
    }
}
