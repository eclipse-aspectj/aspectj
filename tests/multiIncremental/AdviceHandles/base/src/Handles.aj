package spacewar;

//=AdviceHandles/src<spacewar*Handles.aj}Handles
public aspect Handles {

	// =AdviceHandles/src<spacewar*Handles.aj}Handles&before
	before() : execution(* *..*()) {
		
	}
	// =AdviceHandles/src<spacewar*Handles.aj}Handles&before!2
	before() : execution(* *..*()) {
		
	}
	// =AdviceHandles/src<spacewar*Handles.aj}Handles&before&I
	before(int x) : execution(* *..*(int)) && args(x) {
		
	}
	
	// =AdviceHandles/src<spacewar*Handles.aj}Handles&before&I!2
	before(int x) : execution(* *..*(int)) && args(x) {
		
	}

	// =AdviceHandles/src<spacewar*Handles.aj}Handles&after
	after() : execution(* *..*()) {
		
	}
	
	// =AdviceHandles/src<spacewar*Handles.aj}Handles&afterReturning
	after() returning() : execution(* *..*()) {
		
	}

	// =AdviceHandles/src<spacewar*Handles.aj}Handles&afterThrowing
	after() throwing(): execution(* *..*()) {
		
	}

	// =AdviceHandles/src<spacewar*Handles.aj}Handles&afterThrowing&I
	after(int x) throwing(): execution(* *..*(int)) && args(x) {
		
	}

        int x() {
          // =AdviceHandles/src<spacewar*Handles.aj}Handles~x[NamedClass
          class NamedClass {
            void doIt() {
            }
          }
          return 0;
        }

  interface I {}
  public void foo() {
    new I() {};
    new I() {};
  }
       

//Line60: 247742: c3 
void b(String x) {
  new Condition() {}; // l62
  new Condition() {}; // l63
}
void c(String x) {
  new Condition() {}; // l66
  new Fondue() {}; // l67
}


}

class Condition {} class Fondue {}

aspect Foo { // Line 71: 247742: c4
 after(String x) returning(String y) : within(NotExisting*) {  } //src<p*Foo.aj[Foo*&afterReturning&QString;
 after(String x) returning(int y) : within(NotExisting*) {  }    //src<p*Foo.aj[Foo*&afterReturning&QString;!2
}
