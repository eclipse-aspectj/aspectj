package test;

public aspect Profiling {
       pointcut profile(): execution(* *.*(..)) ;

       private pointcut scope() :
                       if(condition())
                       //&& !(execution(* *.condition())) <- uncomment and infinite loop disappears
                       && !cflow(execution(* *.condition()));

       public static boolean condition(){
               return (Math.random()<2); //always true
       }
       before(): profile() && scope() {
               System.out.println("Entering method "+thisJoinPointStaticPart.getSignature());
       }
}
