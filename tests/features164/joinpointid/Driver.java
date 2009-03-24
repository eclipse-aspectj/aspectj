public class Driver {
  public static void main(String[]argv) {
    new A().mone();
    new A().mtwo();
    new A().mone();
    new A().mtwo();
    new A().mone();
    new A().mtwo();
  }
} 

class A {
  public void mone() {}
  public void mtwo() {}
}

aspect X pertypewithin(*) {

  int[] state = new int[5];

  before(): execution(* A.*(..)) {
    int id = thisJoinPointStaticPart.getId();
    System.out.println("At "+thisJoinPoint.getSignature()+" id="+id+" state="+state[id]);
    state[id]++;
  }
}
