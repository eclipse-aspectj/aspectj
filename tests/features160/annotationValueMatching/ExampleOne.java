
enum TraceLevel { NONE, LEVEL1, LEVEL2, LEVEL3 }

@interface Trace {
  TraceLevel value() default TraceLevel.NONE;
}
  
aspect X {
  before(): execution(@Trace !@Trace(TraceLevel.NONE) * *(..)) {
    System.err.println("tracing "+thisJoinPoint);
  }
}

public class ExampleOne {

  public static void main(String[] args) {
    ExampleOne eOne = new ExampleOne();
    eOne.m001();
    eOne.m002();
    eOne.m003();
    eOne.m004();
    eOne.m005();
    eOne.m006();
    eOne.m007();
  }

  @Trace(TraceLevel.NONE)
  public void m001() {}

  @Trace(TraceLevel.LEVEL2)
  public void m002() {}

  @Trace(TraceLevel.LEVEL3)
  public void m003() {}

  @Trace(TraceLevel.NONE)
  public void m004() {}

  @Trace(TraceLevel.LEVEL2)
  public void m005() {}

  @Trace(TraceLevel.NONE)
  public void m006() {}

  @Trace
  public void m007() {}

}
