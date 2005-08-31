public aspect EnumTest {
    public static enum Test {
	A,B,C
    }

    public void et.Q.foo(Test t) {
	switch (t) {
	case B:
		System.out.println("B!");
	    break;
	}
    }
    
    public static void main(String[] args) {
    	et.Q q = new et.Q();
    	q.foo(Test.B);
    	q.foo(Test.C);
    }
}