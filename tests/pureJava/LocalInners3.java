public class LocalInners3 {
    public static void main(String[] args) {
	class Inner {
	    class A extends B {}
	    class B extends Inner {}
	}
	return;
    }
}
