public aspect HasPrivateMethodInherited {
	
	declare parents : D && hasmethod(* print(..)) implements Printable; 

	public static void main(String[] args) {
		C c = new C();
		if ((c instanceof Printable)) {
			throw new RuntimeException("declare parents : hasmethod failed on super");
		}
		D d = new D();
		if ((d instanceof Printable)) {
			throw new RuntimeException("declare parents : hasmethod failed on sub");
		}
		
	}
}

class C {
	
	private void print() {}
	
}

class D extends C {
	
}

interface Printable {};