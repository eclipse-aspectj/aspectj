import java.util.*;
import org.aspectj.lang.annotation.*;

@Aspect
class Iffy2 {

  @Before("execution(!void *(..))")
  public void advice1() {}

  @Before("execution(!void[] *(..))")
  public void advice2() {}

  @Before("execution(!void *(..))")
  public void advice3() {}

  @Before("execution(*..Collection[] *(..))")
  public void advice4() {}

  @Before("execution(java.util.Collection<?>[] *(..))")
  public void advice5() {}

  /**
   * TODO: This pointcut is not parsed correctly. Obviously, the combination of
   * '*' and '&lt;?&gt;' leads to an AJ core dump with this error message:
   * <p>
   * <code>
   *   org.aspectj.weaver.BCException: malformed org.aspectj.weaver.PointcutDeclaration attribute (length:219)
   *   org.aspectj.weaver.BCException: Bad type signature *
   *     at org.aspectj.weaver.AjAttribute.read(AjAttribute.java:137)
   *     at org.aspectj.weaver.bcel.Utility.readAjAttributes(Utility.java:102)
   *     at org.aspectj.weaver.bcel.BcelMethod.unpackAjAttributes(BcelMethod.java:197)
   *     at org.aspectj.weaver.bcel.BcelMethod.&lt;init&gt;(BcelMethod.java:91)
   *     at org.aspectj.weaver.bcel.BcelObjectType.getDeclaredMethods(BcelObjectType.java:290)
   *     at org.aspectj.weaver.ReferenceType.getDeclaredMethods(ReferenceType.java:870)
   *     at org.aspectj.weaver.ResolvedType.getDeclaredAdvice(ResolvedType.java:1028)
   *     at org.aspectj.weaver.ResolvedType.getDeclaredShadowMungers(ResolvedType.java:1068)
   *     at org.aspectj.weaver.ResolvedType.collectShadowMungers(ResolvedType.java:868)
   *     at org.aspectj.weaver.ResolvedType.collectCrosscuttingMembers(ResolvedType.java:794)
   *     at org.aspectj.weaver.CrosscuttingMembersSet.addOrReplaceAspect(CrosscuttingMembersSet.java:112)
   *     at org.aspectj.weaver.CrosscuttingMembersSet.addOrReplaceAspect(CrosscuttingMembersSet.java:67)
   *     at org.aspectj.weaver.bcel.BcelWeaver.prepareForWeave(BcelWeaver.java:512)
   * </code>
   */
  //@Before("execution(*..Collection<?>[] *(..))")
  public void advice6() {}

  public Collection<?>[] getCollectionArray() {
        return null;
  }
}
