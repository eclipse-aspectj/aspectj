import java.util.*;

public class TestB_class {
  public static void main(String []argv) { 
    TestB_generictype<Float,String>  sc1=new TestB_generictype<Float,String>();
    TestB_generictype<Integer,Float> sc2=new TestB_generictype<Integer,Float>();

    List<Integer> li = new ArrayList<Integer>();
    List<String>  ls = new ArrayList<String>();
    List<Float>   lf = new ArrayList<Float>();

    sc1.mxy(li,lf,ls,li);
    sc2.myx(li,lf,li,li);
  }
}
