import org.aspectj.testing.Tester;

public class InterFieldArrays {
	public static void main(String[] args) {
		Foo foo = new Foo();
		Tester.checkEqual(foo.bar.length, 3);
		Tester.checkEqual(foo.bar1.length, 3);
		
		foo.bar2 = new int[] { 21, 22, 23};
		Tester.checkEqual(foo.bar2.length, 3);
		
		Tester.checkEqual(foo.bar[2], 3);
		Tester.checkEqual(foo.bar1[2], 13);
		Tester.checkEqual(foo.bar2[2], 23);
		
		int[] a = foo.getInts();
	}
}

class Foo { }
aspect Bar {
  int[] Foo.bar = { 1, 2, 3 };
  int[] Foo.bar1 = new int[] { 11, 12, 13};
  int[] Foo.bar2 = null;
  
  int[] Foo.getInts() { return new int[] { 1, 2, 3}; }
}