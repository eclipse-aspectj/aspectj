public class SwallowedException {
	
	public void foo() throws Exception {
		throw new Exception("ta da!");
	}
	
	public void bar() {
		try {
			foo();
		}
		catch(Exception ex) {
			// yum yum
		}
	}
	
	
}