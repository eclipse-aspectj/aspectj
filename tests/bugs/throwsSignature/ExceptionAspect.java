public aspect ExceptionAspect
{
    pointcut exceptionThrower() :
        execution(public * ExceptionBugTest.*(..) throws Exception+);

    declare warning : exceptionThrower() : "throws both";
    
    declare error : execution(public * ExceptionBugTest.*(..) throws Exception) : 
        "throws Exception";
}