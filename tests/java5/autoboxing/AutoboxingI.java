public class AutoboxingI {
	
  public static void method_takes_Integer(Integer i) { System.err.println("method_takes_Integer="+i);}
  public static void method_takes_int(int i) {         System.err.println("method_takes_int="+i);}
  
  public static void main(String[] argv) {
    Integer one   = new Integer(10000);
    int two       = 20000;
    Integer three = new Integer(30000);
    int four      = 40000;
    method_takes_Integer(one);
    method_takes_Integer(two);
    method_takes_int(three);
    method_takes_int(four);
  }
}
