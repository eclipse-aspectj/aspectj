import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface I {}

aspect X {
  declare warning: execution(* *(@(@I *) *)): "";
}

public class A {
  public void foo(@I boolean[] bs) {}
}
