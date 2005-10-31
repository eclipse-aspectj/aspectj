// "no XLint warning: thisJoinPoint potentially lazy and nothing stopping it"

public aspect Scenario1 {

    public static boolean enabled = true;

    pointcut toBeTraced() : execution(* main(..));

    before () : toBeTraced() && if(enabled) {
        Object[] args = thisJoinPoint.getArgs(); // tjp made lazily
        System.out.println(thisJoinPoint + ", arg's: " + args.length);
    }

}

class Test{
  static void main(String [] args){
  }
}
