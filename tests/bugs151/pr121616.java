import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

class Main {
        public static void main(String[] args) {
                System.out.println("Main");
        }
}

@Aspect
class MainLogger {
    @Before("execution(* main(..))")
    public void log(JoinPoint thisJoinPoint) {
            System.out.println("Before " + thisJoinPoint);
    }
}
