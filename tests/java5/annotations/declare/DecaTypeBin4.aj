import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();} 

public aspect DecaTypeBin4 {
  declare @type: AnnotatedType : @Fruit("Banana");
}

aspect X {
  before(): execution(* *(..)) && @this(Color) {
    System.err.println("Color identified on "+thisJoinPoint);
  }

  before(): execution(* *(..)) && @this(Fruit) {
    System.err.println("Fruit identified on "+thisJoinPoint);
  }
}
