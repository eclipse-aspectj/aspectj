package TestRequirements;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
@interface AnnotatedMethod {}

@Retention(RetentionPolicy.RUNTIME)
@interface NewAnnotatedMethod {
    boolean newValue();
}

aspect X {
    declare @method: !@AnnotatedMethod * TestRequirements.*(..) : @NewAnnotatedMethod(newValue = true);
}

public class TestRequirements {
    @AnnotatedMethod
    public void dontMatchMe() {}

    public void matchMe() {}

  public static void foo() throws Exception {
    if (TestRequirements.class.getDeclaredMethod("dontMatchMe").getAnnotation(NewAnnotatedMethod.class)!=null) {
      throw new IllegalStateException();
    }
    if (TestRequirements.class.getDeclaredMethod("matchMe").getAnnotation(NewAnnotatedMethod.class)==null) {
      throw new IllegalStateException();
    }
  }
}
