public class MissingExposure {
    public static void main(String[] args) {
        C c = new C();
        org.aspectj.testing.Tester.check(false, "shouldn't have compiled");
    }
}

class C {
    int _int = 13;
}

aspect Aspect {
    pointcut crash(byte _new): set(int C._int) && args(int);
    before(byte _new): crash(_new) {}
}
