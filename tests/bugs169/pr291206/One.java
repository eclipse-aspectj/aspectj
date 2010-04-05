import java.lang.annotation.*;

aspect X {
  declare warning: @Anno *: "Nothing should be annotated Anno!";
}

@Anno 
class C {
  
}

class D {
  
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}
