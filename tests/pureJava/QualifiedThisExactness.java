class Nimbo {}

public class QualifiedThisExactness extends Nimbo {
    class Goo {
	void main() {
	    System.err.println(Nimbo.this);
	}
    }
}
