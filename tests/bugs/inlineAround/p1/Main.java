package p1;

public class Main {
	public static void main(String[] args) {
		new Main().doit();
	}
	
	private void doit() {
		long l = 2l + testit(3.2, new C().doit(23, 3.14), 5l);
		System.err.println(l);
	}
	
	private long testit(double d, long l1, long l2) {
		return (long)(d + l1 + l2);
	}
}

class C {
	long doit(int i, double d) {
		return (long)(d + i + f1(d, i, i));
	}
	
	int f1(double d1, long l1, int i1) {
		return (int)(d1 + l1 + i1);
	}
}