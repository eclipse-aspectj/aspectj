// "no XLint warning: thisJoinPoint cannot be built lazily"

public aspect Scenario4 {

    public static boolean enabled = true;

    pointcut toBeTraced() : execution(* main(..));

    before () : toBeTraced() { // no if condition
        Object[] args = thisJoinPoint.getArgs(); // tjp not made lazily
        System.out.println(thisJoinPoint + ", arg's: " + args.length);
    }

}

class Test{
  static void main(String [] args){
  }
}
