
public aspect SimpleF {
    public static void main(String[] args) { test(); }

    pointcut sendHeader():
        call(void *.*(String));

/*
    before(): call(* foo(..)) {
                aspectField += s;
    }
*/

    public static void test() {
    }
}
