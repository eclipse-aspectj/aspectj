import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Meta {}

@Retention(RetentionPolicy.RUNTIME)
@Meta
@interface Foo {}

public class Code {

  @Foo
  public void m() {}
}
aspect X {
  before(): execution(@@Meta * *(..)) {
  }
}

