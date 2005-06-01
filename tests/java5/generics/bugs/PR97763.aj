import java.util.Vector;

public class PR97763 {
  Vector<String> v = new Vector<String>();
  Vector<Object> vo;

  public void mStr(Vector<String> v1) {v1.add("hello");}
  public void mInt(Vector<Integer> v1) { }

  public static void main(String []argv) {
    PR97763 p = new PR97763();
    p.mStr(p.v);
    p.itdmStr(p.v);
    System.err.println("Number of entries="+p.v.size());
  }
}

aspect FooAspect {
  public void PR97763.itdmObj(Vector<Object> v) { }
  public void PR97763.itdmStr(Vector<String> v) { v.add("world");}
}
