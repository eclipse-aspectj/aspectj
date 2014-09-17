public class Coo {
	Coo() {	
	}
	
	public static void main(String[] args) {
	}
}

aspect Azpect {
    before(): !cflow(preinitialization(Coo.new(..))) && execution(* main(..)) {  }
}

