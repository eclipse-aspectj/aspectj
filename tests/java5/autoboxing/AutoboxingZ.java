public class AutoboxingZ {
	
  public static void method_takes_Boolean(Boolean b) { System.err.println("method_takes_Boolean="+b);}
  public static void method_takes_boolean(boolean b) { System.err.println("method_takes_boolean="+b);}
  
  public static void main(String[] argv) {
    Boolean t   = new Boolean(false);
    boolean f   = false;
    method_takes_Boolean(t);
    method_takes_Boolean(f);
    method_takes_boolean(t);
    method_takes_boolean(f);
  }
}
