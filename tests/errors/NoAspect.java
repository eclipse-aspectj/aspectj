public class NoAspect {
    public static void main(String[] args) {
        new NoAspect().go();
    }

    void go() {
    }
}


class A 
{    before(): target(*) && call(* go(..)) {}
}
