public class D {
	public void m() throws Exception {
		clone();
	}
}

class E {}

aspect X {
  before(): target(E) { }
}

