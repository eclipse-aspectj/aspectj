public class Declares {
	public void m() {  // should be error on this line
	}
}

aspect Checker {
    declare error: execution(* m()): "too short method name";
    declare error: execution(* n()): "too short method name";  // no effect
    
    //declare warning: exec: "bar";
}


