import java.lang.annotation.*;

public aspect Two {
  declare parents: @Foo * implements II;
}

interface II {}

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}
