package foo;



public class Foo extends AncientFoo {
    protected int i() { return 42; }
    public static void main(String[] args) {
        new Foo().doStuff();
    }
    public void doStuff() { }
    protected int ancientI() { return 42; }
}

class AncientFoo {
    protected int ancientI() { return -42; }
    protected int ancientJ() { return 0; }
}