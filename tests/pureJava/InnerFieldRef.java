
public class InnerFieldRef {
    private Foo foo = new Foo();

    private static int x;

    public static void main(String[] args) {
	new InnerFieldRef().new Inner().m();
    }
    
    private class Inner {
        public void m() {
            foo.b = true;
	    (new InnerFieldRef()).x = 3;
        }
    }
}

class Foo {
    public boolean b;
}
