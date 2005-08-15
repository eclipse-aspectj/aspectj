import java.util.*;

public aspect CtorG {

  // visibility options...
  public  Base<Z>.new(List<Z> lz,int i) {}
  private Base<Z>.new(List<Z> lz,String s) {}
          Base<Z>.new(List<Z> lz,boolean b) {}

    public static void main(String []argv) {
	    List<Integer> intList = new ArrayList<Integer>();
	    Base b1 = new Base(intList,1);
//	    Base b2 = new Base(intList,"a");
	    Base b3 = new Base(intList,true);
  	}
}

class Base<N extends Number> { 

}
