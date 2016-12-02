public class Test4 {
  public static void main(String[] argv) {
    coloured(1,"abc");
  }
  public static void coloured(int param1, @Filler @ColouredAnnotation(RGB.GREEN) String param2) {}
}

aspect X {
  // execution(@ColouredAnnotation * colouredMethod(..)) && @annotation(ColouredAnnotation(colour));
  before(ColouredAnnotation ca):  execution(* *(..)) && @args(*, ca (*)) {
    System.out.println("Annotation from parameter on method "+thisJoinPoint+" is "+ca);
  }
}
