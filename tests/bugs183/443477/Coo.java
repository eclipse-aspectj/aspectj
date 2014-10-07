public class Coo {
	Coo() {	
	}

  int i = 4;
  
	
	public static void main(String[] args) {
	}
}

aspect Azpect {
  before(): !cflow(preinitialization(Coo.new(..))) && execution(* main(..)) {  }
}

