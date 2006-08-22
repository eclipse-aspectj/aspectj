import java.io.Serializable;
import java.lang.reflect.Field;
import com.testware.ejb.common.*;

// when in a package, AJ worked out:
//Test SerialVersionUID is 8593816477552447372
//ATest SerialVersionUID is -5439116922937363745
// atest serialveruid: -5439116922937363745L
//  test serialveruid: 8593816477552447372L


//ATest: static final long serialVersionUID = 9091955077097551023L;
//Test: static final long serialVersionUID =  1583992244946994789L;

//ATest SerialVersionUID is 9091955077097551023
//Test SerialVersionUID is 1583992244946994789
//
aspect X {
	before(): staticinitialization(*Test) {}
}

public class TwoTypes implements Serializable {
  public static void main(String[] args) {
	  try {
		  Test c = (Test)Test.class.newInstance();
		  Field f = Test.class.getDeclaredField("serialVersionUID");
		  f.setAccessible(true);
		  long l = f.getLong(c);
	      System.err.println("Test SerialVersionUID is "+l);
	  
	      
		 // ATest b = (ATest)ATest.class.newInstance();
	      f = ATest.class.getDeclaredField("serialVersionUID");
	      f.setAccessible(true);
	      l = f.getLong(Test.class.getSuperclass());
	      System.err.println("ATest SerialVersionUID is "+l);

	  
	  } catch (Exception e) {
	      System.err.println("Problem: "+e.toString());
	      e.printStackTrace();
	  }
  }
//  
//  public int anInt;
//  
//  public static boolean aBoolean = false;
//  
//  public long foo = 376;
//  
//  public void m() {}
//  public int compareTo(Object o) { return 0;}
//  public String m2(boolean b,long l, String s) { return "";}
//  
//  public static transient short fo2 = 3;
  
}