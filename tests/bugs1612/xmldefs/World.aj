import org.aspectj.lang.Signature;
import org.aspectj.lang.JoinPoint;
public abstract aspect World { 
    //private Object result;
        pointcut greeting() : execution(* Hello.sayWorld(..)); 

        Object around(): greeting() {
        System.out.println("around start!");
        Object result = proceed();
        System.out.println("around end!");
        return result;
        }

//    before() : greeting() { 
//      Signature signature = thisJoinPoint.getSignature();
//        System.out.println("before " + signature.getName()); 
//    } 

//    after() returning () : greeting() { 
//      Signature signature = thisJoinPoint.getSignature();
//        System.out.println("after " + signature.getName()); 
//    } 

} 
