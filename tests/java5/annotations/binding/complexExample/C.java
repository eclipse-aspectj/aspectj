package g.h.i;
import d.e.f.*;

@Color("black")
public class C {

  public static void main(String []argv) {
    if (!(new C() instanceof java.io.Serializable))
      throw new RuntimeException("C should be serializable, done via decp");
  }

  public void c() { System.err.println("g.h.i.C.c running");}
}
