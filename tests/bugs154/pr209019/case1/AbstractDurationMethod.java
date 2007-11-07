package c.d;

public abstract aspect AbstractDurationMethod {
   public abstract pointcut methods();

   Object around(): methods() {
       Object o = proceed();
       System.out.println("Proceeded at joinpoint "+thisJoinPoint);
       return o;
   }
}
