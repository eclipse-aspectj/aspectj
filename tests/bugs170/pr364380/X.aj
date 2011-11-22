package ca;
import org.aspectj.lang.annotation.*;

@Aspect
class OwnershipSecurityAspect {


   @Pointcut("call(public void ca..setOwner(..)) && !within(ca..OwnershipSecurityAspect) && !within(ca..*Test)")
   protected void myPcut() {}

   @DeclareWarning("myPcut()")
   public static final String securityError = "An advice already exists for setting an owner";



}
