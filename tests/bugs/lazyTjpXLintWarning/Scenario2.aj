// "XLint warning: thisJoinPoint potentially lazy but stopped by around advice which doesn't use tjp"

public aspect Scenario2 {

    public static boolean enabled = true;

    pointcut toBeTraced() : execution(* main(..));

    before () : toBeTraced() && if(enabled) {
        Object[] args = thisJoinPoint.getArgs(); // tjp not made lazily
        System.out.println(thisJoinPoint + ", arg's: " + args.length);
    }

    Object around() : toBeTraced() && if(enabled) {
        return proceed();
    }
	
}

class Test{
  static void main(String [] args){
  }
}
