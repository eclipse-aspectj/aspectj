package p;

aspect InnerTest {
    private interface Inner {}
    declare parents: other.Test implements Inner;

    private int Inner.count = 0;

    before(Inner i): target(i) && call(* *(..)) {
        i.count++;
    }

    public static int getCallCount(Object o) {
        if (o instanceof Inner) return ((Inner)o).count;
        else return -1;
    }
}
