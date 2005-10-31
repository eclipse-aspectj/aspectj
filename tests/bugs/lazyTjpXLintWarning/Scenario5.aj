//"XLint warning: thisJoinPoint potentially lazy but stopped by around advice which uses tjp"

public aspect Scenario5 {

    public static boolean enabled = true;

    pointcut toBeTraced() : execution(* main(..));

    before () : toBeTraced() && if(enabled) {
        Object[] args = thisJoinPoint.getArgs(); // tjp not made lazily
        System.out.println(thisJoinPoint + ", arg's: " + args.length);
    }

    Object around() : toBeTraced() && if(enabled) {
      Object[] args = thisJoinPoint.getArgs(); // tjp used in the around advice
      return proceed();
    }
	
}

class Test{
  static void main(String [] args){
  }
}
