public class GenericSuper<T> {

	public void doSomethingWith(T t) {
		System.out.println("with");
		System.out.println(t.toString());
	}
	
	public void doSomethingElseWith(T t) {
		System.out.println("else");
		System.out.println(t);
	}
	
}
