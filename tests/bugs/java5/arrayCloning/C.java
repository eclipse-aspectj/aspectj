import java.lang.reflect.*;

public class C {

  public static B.D[] arr = new B.D[5];

  public static void main(String[]argv) {
    arr[0] = new B.D(42);
    arr[1] = new B.D(22);
    arr[2] = new B.D(46);
    arr[3] = new B.D(50);
    arr[4] = new B.D(54);

    B.D[] arr2 = arr.clone();


    // Check the clone is OK
    if (arr2[0].i!=42) throw new RuntimeException("Call that a clone 0");
    if (arr2[1].i!=22) throw new RuntimeException("Call that a clone 1");
    if (arr2[2].i!=46) throw new RuntimeException("Call that a clone 2");
    if (arr2[3].i!=50) throw new RuntimeException("Call that a clone 3");
    if (arr2[4].i!=54) throw new RuntimeException("Call that a clone 4");
    System.err.println("Clone OK - attempting value manipulation");

    // Change the clone, check the original is OK
    arr2[2] = new B.D(1);
    if (arr[2].i == 1)  throw new RuntimeException("Shouldnt have affected original");
    if (arr2[2].i != 1) throw new RuntimeException("Should have affected clone");


    System.err.println("Clone OK - finished");
  }
}


class B {
  public static class D {
    public int i;
    D(int x) { i=x;}
  }
}
