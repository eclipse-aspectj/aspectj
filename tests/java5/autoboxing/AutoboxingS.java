public class AutoboxingS {
	
  public static void method_takes_Short(Short i) { System.err.println("method_takes_Short="+i);}
  public static void method_takes_short(short i) { System.err.println("method_takes_short="+i);}
  
  public static void main(String[] argv) {
    Short one   = new Short("100");
    short two   = 200;
    Short three = new Short("300");
    short four  = 400;
    method_takes_Short(one);
    method_takes_Short(two);
    method_takes_short(three);
    method_takes_short(four);
  }
}
