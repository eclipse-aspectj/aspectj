public class AutoboxingF {
	
  public static void method_takes_Float(Float i) { System.err.println("method_takes_Float="+i);}
  public static void method_takes_float(float i) {         System.err.println("method_takes_float="+i);}
  
  public static void main(String[] argv) {
    Float one   = new Float(100.0f);
    float two       = 200.0f;
    Float three = new Float(300.0f);
    float four      = 400.0f;
    method_takes_Float(one);
    method_takes_Float(two);
    method_takes_float(three);
    method_takes_float(four);
  }
}
