
class Super {
    static void goo() {}
    static int getInt() { return 42; }
}

public class SuperIsWeird extends Super {
    static void foo0() {
        super.goo();  // error
    }

    void foo1() {
        Object o = super; // error
    }

    void foo2() {
        super.goo(); // no error
    }

    static int v0 = super.getInt(); // error
    Object v1 = super; // error
    int v2 = super.getInt(); // no error
}

