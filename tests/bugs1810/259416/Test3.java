public class Test3 {
  public static void main(String[] argv) {
    coloured(1,"abc");
  }
  public static void coloured(int param1, @ColouredAnnotation(RGB.RED) String param2) {}
}

aspect X {
  // execution(@ColouredAnnotation * colouredMethod(..)) && @annotation(ColouredAnnotation(colour));
  before(ColouredAnnotation ca):  execution(* *(..)) && @args(*, ca (*)) {
    System.out.println("Annotation from parameter on method "+thisJoinPoint+" is "+ca);
  }
}
