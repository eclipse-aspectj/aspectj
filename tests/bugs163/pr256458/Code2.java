import org.aspectj.lang.annotation.*;

@Aspect
public class Code2 {

   @Around("execution(* Code.*(..)) && if(java.lang.System.currentTimeMillis() > 1)")
   public void foo() {
   }

}
