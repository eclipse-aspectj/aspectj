public class SimpleAutoboxing {
	
  public static void method_takes_Integer(Integer i) { System.err.println("method_takes_Integer="+i);}
  
  public static void main(String[] argv) {
    int one     = 20000;
    method_takes_Integer(one);
  }
}
