import java.util.Arrays;


public aspect Asp {
   before() : execution(* *(@Ann (*), ..)) {
      //System.out.println(thisJoinPoint.getSignature().toShortString() + ' ' + Arrays.asList(thisJoinPoint.getArgs()));
      throw new RuntimeException("expected exception");
   }
}
