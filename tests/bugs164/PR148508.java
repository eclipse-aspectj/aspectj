
aspect A {
  pointcut broken1() : execution(* *(Object[]+));
//  pointcut broken2(): execution(* *(*)) && args(Object[]+);

  before(): broken1() { System.out.println("a"); }
//  before(): broken2() { System.out.println("b"); }
}

public class PR148508 {
 
  public static void main(String []argv) {
    PR148508 instance = new PR148508();
    instance.run();
  }
  
  public void run() {
	  Object[] arr = new String[5];
	  boolean b = arr instanceof String[];
    
    
    
    //    instance.m1(new Object[]{});
//    instance.m2(new Integer[]{});
//    instance.m3(new String[]{});
  }

//  public void m1(Object[] os) { }
//  public void m2(Integer[] is) { }
//  public void m3(String[] ss) { }

}
