public class CounterTest05 {

/*
 * Here we have an abstract pointcut that is used within a cflow.  In the two concrete sub-aspects
 * we make the abstract pointcut concrete.  The aim of the test is to ensure we do not share
 * the cflow counter objects, since the pointcut within the cflow() in each case points at a 
 * different 'entry' point.  The count should be 10 when we finish.  If it is 8 we have shared
 * a counter.
 */
  public static void main(String []argv) {
    print();
    print();
    below1();
    System.err.println("ctr="+A.ctr);
    if (A.ctr!=10) 
    	throw new RuntimeException("Counter should be 10 but is "+A.ctr);
  }

  public static void below1() {
    print();
    print();
    below2();
  }

  public static void below2() {
    print();
    print();
  }

public static void print() {}
}

abstract aspect A {
  public static int ctr = 0;

  abstract pointcut abs();

  pointcut p(): call(* print(..)) && cflow(abs());

  before(): p() {
    A.ctr++;
  }
}

aspect B extends A {
  pointcut abs(): execution(* main(..)); // ctr increases by 6
}

aspect C extends A {
  pointcut abs(): execution(* below1(..)); // ctr increases by 4
}
