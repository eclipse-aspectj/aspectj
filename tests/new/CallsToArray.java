import org.aspectj.testing.Tester;

public class CallsToArray {
    public static void main(String[] args) {
	byte[] a = new byte[] {0, 1};
	byte[] b = (byte[])a.clone();

	Tester.check(a != b, "cloned array is different");
	Tester.check(a[0] == b[0] && a[1] == b[1], "but compares equal");

	Tester.check(A.returnedClone == b, "advice did right thing");
    }

}


aspect A {
    static Object returnedClone;
    after () returning(Object cloned): call(Object Object.clone()) {
	System.out.println("running advice");
	A.returnedClone = cloned;
    }
    /*
    static after () returning(Object cloned): calls(Object .clone()) {
	System.out.println("running advice on byte[]");
	A.returnedClone = cloned;
    }
    */
}
