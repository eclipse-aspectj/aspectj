import java.util.*;

public aspect CodeFour {
  
  static final class FinalSet<T> extends HashSet<T> {}

  before(): execution(* *(..)) && args(List<?>) {}
  
  public void m(FinalSet<String> ss) {
    // List<?> l = (List<?>)ss; // cannot write this (FinalSet is final) so pointcut should not match
  }

}
