
/** @testcase PR#600 AbstractMethodError for introduced methods under some orderings of input files */
public class Main {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        C c = new C();

        a.setNext(b);
        b.setNext(c);

        a.doIt();
    }
}
