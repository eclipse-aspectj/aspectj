package protectedFieldRefInInner;

public class Main extends protectedFieldRefInInner.p1.C {
    private int y = 4;
    class Inner {
	int foo() { return x = x + y; }
    }
    public static void main(String[] args) {
	new Main().new Inner().foo();
    }
}
