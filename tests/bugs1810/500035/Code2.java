import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

@Aspect
public class Code2 {
 
    @Around(value = "args(regex, replacement) && target(targetObject) " +
            "&& call(public String String.replaceFirst(String, String)) && this(c)"
//,            argNames = "proceedingJoinPoint,targetObject,regex,replacement,thisJoinPoint"
)
    public String replaceFirstAspect(ProceedingJoinPoint proceedingJoinPoint, Code2 c, String targetObject, String regex, String replacement) throws Throwable {
System.out.println("targetObject = "+targetObject);
System.out.println("regex = "+regex);
System.out.println("replacement = "+replacement);
        String returnObject = (String) proceedingJoinPoint.proceed(new Object[]{ c, targetObject, regex, replacement});
        return returnObject;
    }


  public static void main(String []argv) {
    new Code2().run();
  }

  public void run() {
    String s = "hello";
    s = s.replaceFirst("l","8");
    System.out.println(s);
  }

}
