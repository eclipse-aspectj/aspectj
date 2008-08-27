import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

@Aspect
class X {
	
    @Pointcut("target(p) && args(c) && execution(* foo(..))")
    public void add(Bug p, C c) {} 

    @Around("add(parent, child)")
    public Object addAdvice(ProceedingJoinPoint pjp, Object parent, Object child) throws Throwable {
       // System.out.println(".java - inside:::" + pjp.toLongString());
        //some work
        return pjp.proceed(new Object[] { parent, child });
    }
}

class C {
	
}

public class Bug {
    public static void main(String []argv) {
        new Bug().foo(new C());
    }

    public void foo(C c) {
    }
}
// private static final java.lang.Object foo_aroundBody1$advice(Bug, C, JoinPoint, X, ProceedingJoinPoint, java.lang.Object, java.lang.Object);

/*
Code getting ready to call that:
public void foo(C);
Code:
 Stack=7, Locals=4, Args_size=2
 0:   aload_1
 1:   astore_2
 2:   getstatic       #35; //Field ajc$tjp_0:Lorg/aspectj/lang/JoinPoint$StaticPart;
 5:   aload_0
 6:   aload_0
 7:   aload_2
 8:   invokestatic    #41; //Method org/aspectj/runtime/reflect/Factory.makeJP:(Lorg/aspectj/lang/JoinPoint$StaticPart;Ljava/lang/Object;
Ljava/lang/Object;Ljava/lang/Object;)Lorg/aspectj/lang/JoinPoint;
 11:  astore_3
 12:  aload_0 // THIS
 13:  aload_2 // C
 14:  aload_3 // ProceedingJoinPoint
 15:  invokestatic    #84; //Method X.aspectOf:()LX; // aspectOf (instance of X)
 18:  aload_3 // ProceedingJoinPoint
 19:  aload_0 // THIS
 20:  aload_2 // C
 21:  invokestatic    #88; //Method foo_aroundBody1$advice:(LBug;LC;Lorg/aspectj/lang/JoinPoint;LX;Lorg/aspectj/lang/ProceedingJoinPoint;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
 24:  pop
 25:  return  LineNumberTable:
 line 1: 0
 
 
 Here is foo_aroundBody1$advice:
 
   0:   aload   4  // PJP
   2:   iconst_2   // PJP, 2
   3:   anewarray       #3; //class java/lang/Object // Object[2]
   6:   dup // Object[2] Object[2]
   7:   iconst_0 // O[2] O[2] 0
   8:   aload   5 // O[2] O[2] 0 Bug
   10:  aastore // O[2] with Bug in first position
   11:  dup // O[2] O[2]
   12:  iconst_1 // O[2] O[2] 1
   13:  aload   6 // O[2] O[2] 1 C
   15:  aastore // O[2] with Bug in first position and C in second position
   16:  astore  7 // store o[2] in 7
   18:  astore  8 // store pjp in 8
   20:  aload_0  // Bug
   21:  aload   7 // Bug O[2]
   23:  iconst_0 // Bug O[2] 0
   24:  aaload // Bug Bug
   25:  checkcast       #1; //class Bug
   28:  aload   8 // Bug Bug PJP  == loaded incorrect argument out of the array (and cast it to the wrong type)
   30:  invokestatic    #61; //Method foo_aroundBody0:(LBug;LC;Lorg/aspectj/lang/JoinPoint;)V
   33:  aconst_null
   34:  areturn
  LocalVariableTable:
   Start  Length  Slot  Name   Signature
   0      35      0    this       LX;
   0      35      1    pjp       Lorg/aspectj/lang/ProceedingJoinPoint;
   0      35      2    parent       Ljava/lang/Object;
   0      35      3    child       Ljava/lang/Object;


 
 
*/