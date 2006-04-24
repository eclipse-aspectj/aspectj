import org.aspectj.lang.annotation.*;

@Aspect("perthis(pc())")
public class AtAspectWithPerClause {

 @Pointcut("execution(* *(..))")
 public void pc() {}

}

@Aspect
class Foo {

 @Pointcut("execution(* *(..))")
 public void pc() {}

}