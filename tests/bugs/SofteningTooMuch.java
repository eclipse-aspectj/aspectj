
// pr 48522

public class SofteningTooMuch {
	public static void main(String args[]) {
		throw new Exception("should be a compiler error here");
	}
}

class FooException extends Exception {}

aspect ExcPolicy {
	declare soft: FooException: execution(* SofteningTooMuch.*(..));
}

