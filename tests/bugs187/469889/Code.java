interface A<T> {
	T getValue();
}


/*
abstract class AbstractA<T> implements A<T> {
}
*/

interface B extends A<String> {
	@Override
	default String getValue() {
		return "B";
	}
}


/*
class BImpl extends AbstractA<String> implements B {

}

public class Code {
	public static void main(final String[] args) {
		final A<String> object1 = new BImpl();
		System.out.println(object1.getValue());
	}
}

*/
