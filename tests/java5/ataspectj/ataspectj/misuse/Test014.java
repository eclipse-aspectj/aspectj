// "@Pointcut with garbage string"
package ataspectj.misuse;

import org.aspectj.lang.annotation.*;

@Aspect
public class Test014{
    @Pointcut("call%dddd\n\n\n\n\n\n\n\n\n\n\n%dwdwudwdwbuill817pe;][{\ngrgrgnjk78877&&<:{{{+=``\"")
    public void somecall(){
    }

    @Before("fhfh()")
    public void beforeA() {}

    @After("fhfh()")
    public void afterA() {}

    @Around("fhfh()")
    public Object aroundA() {return null;}

    @AfterThrowing(value = "fhfh()", pointcut = "wups()")
    public void afterAT2() {}

    @AfterThrowing("fhfh()")
    public void afterAT() {}

    @AfterReturning(value = "fhfh()", pointcut = "wups()")
    public void afterAR2() {}

    @AfterReturning("fhfh()")
    public void afterAR() {}

    @DeclareError("execution(* Foo.bar())")
    private int X;

    @DeclareWarning("execution(* Foo.bar())")
    private final static String X2 = getX2();

    static String getX2() {return "not supported";}
}
