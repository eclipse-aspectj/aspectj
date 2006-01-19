// let's look at using it for real...

public class Eleven {
	
  static int[] is1,is2;

  public static void main(String []argv) {
    is1 = new int[5];
    is2 = new int[8];   
  }
}

aspect X {
	
  Object interestingArray = null;

  after(int size) returning(Object o): call(*.new(..)) && within(Eleven) && args(size) {
    if (size==8) {
	   System.err.println("Found the interesting array");
	   interestingArray = o;
	}
  }
  
}