public class BadAround {

}

class C {
    public String m(String s) { return "hi"; }
    public int mi() { return 2; }
}

aspect A {
    Object around(): call(String C.m(..)) {
        return new Integer(2);
    }
    Object around(Object a): call(String C.m(..)) && args(a) {
        return proceed(new Integer(2));
    }

    Object around(): call(int C.mi()) {
        return "2";
    }

    int around(): call(String C.m(..)) { // ERR, return type mismatch
        return 2;
    }
}
