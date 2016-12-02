public class Test1 {
  public static void main(String[] argv) {
    coloured("abc");
  }
  public static void coloured(@ColouredAnnotation(RGB.RED) String param1) {}
}

aspect X {
  // execution(@ColouredAnnotation * colouredMethod(..)) && @annotation(ColouredAnnotation(colour));
  before(ColouredAnnotation ca):  execution(* *(@ColouredAnnotation (*))) && @args(ca (*)) {
    System.out.println("Annotation from parameter on method "+thisJoinPoint+" is "+ca);
  }
}
