
package example.aspect;

import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;

import example.dep.Dep;

@Aspect("pertarget(setFieldValue(example.dep.Dep))")
public class FooAspect {

  // interface ajcMightHaveAspect { }

  @Pointcut("set(private * example.dep.Dep.*) && target(dep)")
  public void setFieldValue(Dep dep) {}
  //pointcut setFieldValue(Dep dep) : set(private * Dep.*) && target(dep);

  @Around("setFieldValue(dep)")
  public void foo(Dep dep, ProceedingJoinPoint pjp) {
  //void around(Dep dep) : setFieldValue(dep) {
System.out.println("advised");
    pjp.proceed(new Object[]{dep});
  }

}
