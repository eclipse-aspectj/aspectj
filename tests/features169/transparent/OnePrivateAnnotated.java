import java.lang.annotation.*;

public class OnePrivateAnnotated {
}

aspect X {
  @Anno
  private int OnePrivateAnnotated.x;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}
