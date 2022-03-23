public aspect MarkerAAspect {
  declare precedence : MarkerAAspect, MarkerBAspect;

  Object around() : @annotation(MarkerA) && execution(* *(..)) {
    System.out.println(">> Outer intercept");
    Object result = null;
    for (int i = 0; i < Application.proceedTimesOuter; i++) {
      System.out.println("  >> Outer proceed");
      result = proceed();
      System.out.println("  << Outer proceed");
    }
    System.out.println("<< Outer intercept");
    return result;
  }
}
