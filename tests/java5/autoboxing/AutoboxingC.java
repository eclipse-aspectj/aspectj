public class AutoboxingC {
	
  public static void method_takes_Character(Character i) { System.err.println("method_takes_Character="+i);}
  public static void method_takes_char(char i) {         System.err.println("method_takes_char="+i);}
  
  public static void main(String[] argv) {
    Character one   = new Character('1');
    char two       = '2';
    Character three = new Character('3');
    char four      = '4';
    method_takes_Character(one);
    method_takes_Character(two);
    method_takes_char(three);
    method_takes_char(four);
  }
}
