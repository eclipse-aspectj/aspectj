// "no XLint warning: thisJoinPoint not lazy (no if PCD) but would have been stopped anyway by around advice"

public aspect Scenario3 {

    public static boolean enabled = true;

    pointcut toBeTraced() : execution(* main(..));

    Object around() : toBeTraced() {
	//Object[] args = thisJoinPoint.getArgs();
        return proceed();
    }
	
    before () : toBeTraced(){ // no if condition so tjp not made lazily
        Object[] args = thisJoinPoint.getArgs(); // tjp not made lazily
        System.out.println(thisJoinPoint + ", arg's: " + args.length);
    }
	
}

class Test{
  static void main(String [] args){
  }
}
