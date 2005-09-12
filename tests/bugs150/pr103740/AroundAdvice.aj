import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AroundAdvice { }

aspect ErrorHandling {
  before(): !@annotation(AroundAdvice) && execution(* C.*(..)) { }
}

class C {
  public static void m1() {}
  @AroundAdvice public static void m2() {}
  public void m3() {}
  @AroundAdvice public void m4() {}
}
