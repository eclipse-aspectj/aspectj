import java.io.Serializable;
import java.lang.reflect.Field;

public class BigHorribleClass implements Serializable,Comparable {
  public static void main(String[] args) {
	  try {
		  BigHorribleClass b = (BigHorribleClass)BigHorribleClass.class.newInstance();
	      Field f = BigHorribleClass.class.getDeclaredField("serialVersionUID");
	      long l = f.getLong(b);
	      System.err.println("SerialVersionUID is "+l);
	  } catch (Exception e) {
	      System.err.println("Problem: "+e.toString());
	  }
  }
  
  public int anInt;
  
  public static boolean aBoolean = false;
  
  public long foo = 376;
  
  public void m() {}
  public int compareTo(Object o) { return 0;}
  public String m2(boolean b,long l, String s) { return "";}
  
  public static transient short fo2 = 3;
  
}