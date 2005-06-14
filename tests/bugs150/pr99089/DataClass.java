import java.util.Vector;

public class DataClass {
  private Vector<Object> v = new Vector<Object>();
  private Vector<Object> getV() { return v; }

  public static void main(String[]argv) {
    DataClass dc = new DataClass();
    dc.v.add("hello");
    dc.doit();
  }

  public void doit() {
    v.add("world");
  }
}
