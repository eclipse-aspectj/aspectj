public class NestedConstructionsOfLocalClasses {
    public static void main(String[] args) {
	class D {
	    void x() {
		new D();
	    }
	}
    }
}
