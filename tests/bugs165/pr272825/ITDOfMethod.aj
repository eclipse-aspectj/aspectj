public aspect ITDOfMethod {

	public void Concrete.doSomethingWith(Integer i) {
		System.out.println("In ITD method");
		super.doSomethingWith(i);
	}
	
}
