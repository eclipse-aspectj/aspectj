public class AutoboxingD {
	
  public static void method_takes_Double(Double i) { System.err.println("method_takes_Double="+i);}
  public static void method_takes_double(double i) {         System.err.println("method_takes_double="+i);}
  
  public static void main(String[] argv) {
    Double one   = new Double(100.0f);
    double two       = 200.0f;
    Double three = new Double(300.0f);
    double four      = 400.0f;
    method_takes_Double(one);
    method_takes_Double(two);
    method_takes_double(three);
    method_takes_double(four);
  }
}
