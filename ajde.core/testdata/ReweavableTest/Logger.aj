

public aspect Logger {
  
  after(): call(* approximate(..)) {
  	if (CalculatePI.iteration%10000==0) 
  	  System.out.println("Approximation is now:"+
  	    (CalculatePI.inCircle/CalculatePI.inSquare)*4.0f);
  }
  
}