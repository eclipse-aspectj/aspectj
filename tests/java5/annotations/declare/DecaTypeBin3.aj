import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Color { String value();} 
@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();} 

public aspect DecaTypeBin3 {
  declare @type: A : @Color("Yellow");
  declare @type: A : @Fruit("Banana");
}

aspect X {
  before(): execution(* *(..)) && @this(Color) {
    System.err.println("Color identified on "+thisJoinPoint);
  }

  before(): execution(* *(..)) && @this(Fruit) {
    System.err.println("Fruit identified on "+thisJoinPoint);
  }
}
