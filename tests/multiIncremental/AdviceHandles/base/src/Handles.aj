package spacewar;

//=Spacewar Example/src<spacewar*Handles.aj}Handles
public aspect Handles {

	// =Spacewar Example/src<spacewar*Handles.aj}Handles&before
	before() : execution(* *..*()) {
		
	}
	// =Spacewar Example/src<spacewar*Handles.aj}Handles&before!2
	before() : execution(* *..*()) {
		
	}
	// =Spacewar Example/src<spacewar*Handles.aj}Handles&before&I
	before(int x) : execution(* *..*(int)) && args(x) {
		
	}
	
	// =Spacewar Example/src<spacewar*Handles.aj}Handles&before&I!2
	before(int x) : execution(* *..*(int)) && args(x) {
		
	}

	// =Spacewar Example/src<spacewar*Handles.aj}Handles&after
	after() : execution(* *..*()) {
		
	}
	
	// =Spacewar Example/src<spacewar*Handles.aj}Handles&afterReturning
	after() returning() : execution(* *..*()) {
		
	}

	// =Spacewar Example/src<spacewar*Handles.aj}Handles&afterThrowing
	after() throwing(): execution(* *..*()) {
		
	}

	// =Spacewar Example/src<spacewar*Handles.aj}Handles&afterThrowing&I
	after(int x) throwing(): execution(* *..*(int)) && args(x) {
		
	}

}

