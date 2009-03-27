import org.aspectj.lang.annotation.*;

@Aspect
public class Logger {

        @Before("execution(* O*.*())")
        public void boo() {

        }
        @After("execution(* O*.*())")
        public void aoo() {

        }
        @Around("execution(* O*.*())")
        public void aroo() {

        }

@Pointcut("execution(* *(..))")
public void ooo() {}


@DeclareWarning("execution(* xxx(..))")
public static final String message ="hello";

@DeclareError("execution(* xxx(..))")
public static final String message2 ="gello";

}

