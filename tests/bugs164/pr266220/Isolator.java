public abstract aspect Isolator<T> {

  pointcut scope(): within(T);

  before(): execution(* *(..)) && scope() {
    System.out.println(thisJoinPoint);
  }
}