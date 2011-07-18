import java.lang.annotation.*;

public class Code {

  @Anno
  int i=0;

  @Anno(name="foobar")
  int j=0;

  public void m() {
    i = i+1;
    j = j+1;
  }
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
  String name() default "";
}

aspect X {
  declare warning: get(@Anno(name="") * *) : "name is empty1";
  declare warning: get(@Anno(name="foobar") * *) : "name is empty2";
}
