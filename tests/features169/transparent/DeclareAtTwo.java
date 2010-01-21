import java.lang.annotation.*;

public class DeclareAtTwo {
}

aspect X {
  declare @field: int DeclareAtTwo.x: @Anno2;

  @Anno
  private int DeclareAtTwo.x;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno2 {}
