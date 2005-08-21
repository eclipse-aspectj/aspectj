public aspect HasMethod {
	
	declare parents : hasmethod(* print(..)) implements Printable; 

	public static void main(String[] args) {
		C c = new C();
		if (! (c instanceof Printable)) {
			throw new RuntimeException("declare parents : hasmethod failed");
		}
	}
}

class C {
	
	public void print() {}
	
}

interface Printable {};