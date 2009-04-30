public class Concrete extends GenericSuper<Integer>{

	@Override
	public void doSomethingElseWith(Integer t) {
		System.out.println("In normal method");
		super.doSomethingElseWith(t);
	}
	
}
