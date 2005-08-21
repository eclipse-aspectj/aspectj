public aspect HasFieldInherited {
	
	declare parents : D && hasfield(* printer) implements Printable; 

	public static void main(String[] args) {
		C c = new C();
		if ((c instanceof Printable)) {
			throw new RuntimeException("declare parents : hasfield failed on super");
		}
		D d = new D();
		if (!(d instanceof Printable)) {
			throw new RuntimeException("declare parents : hasfield failed on sub");
		}
		
	}
}

class C {
	
	String printer;
	
}

class D extends C {
	
}

interface Printable {};