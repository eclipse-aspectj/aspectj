import org.aspectj.lang.annotation.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import org.aspectj.lang.*;

@Aspect("perthis(transactional())")
public class Code3 {
   @Pointcut("execution(@Transactional * * (..))")
   public void transactional() { }
   
   @Before("execution(* *(..))")
   public void m(JoinPoint.StaticPart thisJoinPointStaticPart) {
     System.out.println(thisJoinPointStaticPart);
     }
   
   public static void main(String[] args) {
     new AAA().m();
     new BBB().m();
     new CCC().m();
   }
  
}

aspect XXX {
	@Transactional public void CCC.m() {}
}

class AAA { 
  public void m() { }
}

class BBB { 
  public void m() { }
}

class CCC {
}


@Retention(RetentionPolicy.RUNTIME) @interface Transactional {}

