import java.util.*;

public privileged aspect TestAspect {

   pointcut gettingMemberCollection() : get(Set<Number+> *);

   after() : gettingMemberCollection() {
     System.err.println("GO Aspects!  "+thisJoinPoint);
   }
}
