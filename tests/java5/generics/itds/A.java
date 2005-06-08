public class A {
  public static void main(String[] argv) {
    Integer two = 2;
    Integer four= 4;
    System.err.println("min(2,4)=>"+ new Utils().min(two,four));
    System.err.println("max(2,4)=>"+new Utils().max(two,four));
  }
}


aspect X {
  <T extends Number> T Utils.max(T first,T second) {
    if (first.intValue()>second.intValue()) return first; else return second;
  }
}

class Utils {
  <T extends Number> T min(T first,T second) {
    if (first.intValue()<second.intValue()) return first; else return second;
  }
}
