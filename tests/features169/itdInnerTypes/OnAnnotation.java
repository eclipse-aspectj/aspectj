import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
}

aspect X {
  class Anno.Inner {
    Inner() {}
  }
}
