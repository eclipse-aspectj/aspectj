  import org.aspectj.lang.annotation.SuppressAjWarnings;
  
	public aspect SuppressAj {
	
	  pointcut anInterfaceOperation() : execution(* AnInterface.*(..));
	  
	  
	  @SuppressAjWarnings // may not match if there are no implementers of the interface...
	  before() : anInterfaceOperation() {
	     // do something...
	  }		
	  
	  @SuppressAjWarnings("adviceDidNotMatch") // alternate form
	  after() returning : anInterfaceOperation() {
	  	// do something...
	  }
	}
  
  interface AnInterface {
  	 void foo();  	
  }