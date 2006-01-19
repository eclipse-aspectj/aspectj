// writing general advice for multiple array types

public class Twelve {
	
  static int[] is1;
  static Integer[] is2;
  static String[][] strs;

  public static void main(String []argv) {
    is1 = new int[5];
    is2 = new Integer[8];  
    strs = new String[4][8];
  }
}

aspect X {

  after() returning(Object o): call(*.new(..)) && within(Twelve) {
	   System.err.println("It is "+o.getClass());
	   System.err.println("Is it an array? "+o.getClass().isArray());
	   System.err.println("Component type is "+o.getClass().getComponentType());
	   System.err.println("--");
  }
  
}