import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) 
@interface Wibble {
  String message();
}

public class StringBinding {
   
  @Wibble(message="hello world")
  public static void main(String []argv) {
  }
}

aspect X {
  before(String msg): execution(* *(..)) && @annotation(Wibble(msg)) {
    System.out.println(msg);
  }
}
