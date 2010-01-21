import java.lang.annotation.*;

public class OneDeclareAt {
}

aspect X {
  declare @field: int OneDeclareAt.x: @Anno;

  private int OneDeclareAt.x;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}
