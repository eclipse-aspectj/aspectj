import java.util.Map; import java.util.List;
public class A<T> {
  public void m(String s1,java.lang.String s2) {
  }
  public void m2(List l) {}
  public void m3(java.util.ArrayList l) {}

  public void m4(Map<java.lang.String,List> m) {}
  public void m5(java.util.Map<java.lang.String,List> m) {} // parameterized qualified
  public void m6(Map<int[], List> mm) {} // single parameterized with array
  public void m7(int[] mm) {} // primitive array
  public void m8(java.lang.String[] mm) {} 
  public void m9(String[] mm) {} 
  public void m10(List<String>[][] m) {}
  public void m11(java.util.List<T> m) {}
  public void m12(T[] m) {}
  public <T> void m13(Class<T> c, Object f, String d) {}


}
