package pkg;

public aspect A11 {
	
	declare warning: call(* C.setX(..)): "Illegal call.";
    declare warning : execution(* C.setX(..)) : "blah";
}

class C {
	
	public void setX() {
	}
	
	public void method() {
		new C().setX();
	}
	
}
