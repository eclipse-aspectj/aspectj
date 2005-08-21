public aspect HasField {
	
	declare parents : hasfield(* printer) implements Printable; 

	public static void main(String[] args) {
		C c = new C();
		if (! (c instanceof Printable)) {
			throw new RuntimeException("declare parents : hasfield failed");
		}
	}
}

class C {
	
	int printer;
	
}

interface Printable {};