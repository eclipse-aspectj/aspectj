import org.aspectj.lang.annotation.*;

@Aspect
public class AspectClass{

       @Pointcut("call(@Incoming * *(..))")
       public void incomingMessage() {
       }


       @Pointcut("call(@Activity * *(..))")
       public void incomingMessage() {
       }

}