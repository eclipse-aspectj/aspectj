public class AutoboxingB {
	
  public static void method_takes_Byte(Byte i) { System.err.println("method_takes_Byte="+i);}
  public static void method_takes_byte(byte i) {         System.err.println("method_takes_byte="+i);}
  
  public static void main(String[] argv) {
    Byte one   = new Byte("1");
    byte two       = '2';
    Byte three = new Byte("3");
    byte four      = '4' ;
    method_takes_Byte(one);
    method_takes_Byte(two);
    method_takes_byte(three);
    method_takes_byte(four);
  }
}
