import java.lang.annotation.*;

public class OneDefaultAnnotated {
}

aspect X {
  @Anno
  int OneDefaultAnnotated.x;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}
