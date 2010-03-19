public class Errors2 {
	public void is(String s) {
		
	}
}

aspect X {
  before(): execution(* is(..)) {}
}
