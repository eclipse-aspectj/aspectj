// testing what happens with multiple annotations together in a type pattern list @(A B C) type thing


enum Rainbow { RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET }

@interface Col1 { Rainbow value() default Rainbow.RED; }
@interface Col2 { Rainbow value() default Rainbow.YELLOW; }

aspect X {
  before(): execution(@(Col1 && Col2) * *(..)) {
    System.err.println("advising "+thisJoinPoint);
  }
}

public class MultiTypePatterns {

  public static void main(String[] args) {
    MultiTypePatterns eOne = new MultiTypePatterns();
  }

  @Col1 public void m001() {}
  @Col2 public void m002() {}
  @Col1 @Col2 public void m003() {}

}
