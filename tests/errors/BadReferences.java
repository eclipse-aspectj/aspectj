aspect BadReferences {
    public void main(String[] args) {
	int x = C.x;

	Object o = new C.Inner();
    }
}


class C implements I1, I2 {
}

interface I1 {
    public static int x = 1;

    public static class Inner {
    }
}
interface I2 {
    public static int x = 2;
    
    public static class Inner {
    }
}

