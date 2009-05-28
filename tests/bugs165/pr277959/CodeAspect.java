import java.lang.Class;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.*;

public aspect CodeAspect {
    pointcut getPointcut()
      : execution(public * example.AOPTest.cleanup());

    Object around() : getPointcut() {
        System.out.println("ASPECT WORKING");
        //Just call the underlying method
        return proceed();
    }
}
