import java.util.*;

public class Code {
  public String getString()  {
    Optional<String> dummy = Optional.of("Just a dummy optional");
    return dummy.orElseThrow(() ->    {
      return new RuntimeException();
    });
  }
}

aspect X {
  before(): within(!X) {System.out.println(thisJoinPoint);}
}
