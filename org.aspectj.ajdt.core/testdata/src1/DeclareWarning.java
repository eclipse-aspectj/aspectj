public class DeclareWarning {
	public void m() {  // should be error on this line
	}
}

aspect Checker {
    declare warning: execution(* m()): "too short method name";
    declare warning: execution(* n()): "too short method name";  // no effect
}


