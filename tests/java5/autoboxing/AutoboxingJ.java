public class AutoboxingJ {
	
  public static void method_takes_Long(Long i) { System.err.println("method_takes_Long="+i);}
  public static void method_takes_long(long i) {         System.err.println("method_takes_long="+i);}
  
  public static void main(String[] argv) {
    Long one   = new Long(1000000);
    long two       = 2000000;
    Long three = new Long(3000000);
    long four      = 4000000;
    method_takes_Long(one);
    method_takes_Long(two);
    method_takes_long(three);
    method_takes_long(four);
  }
}
