public class ImplicitThisMissing {
    class B {}
    public static void main(String[] args) {
	new B();
	System.err.println("shouldn't compile!!!");
    }
}
