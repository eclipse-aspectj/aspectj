import java.lang.annotation.*;

public class Target {
  public static void main(String []argv) throws Exception {
    Annotation[] annos = Target.class.getDeclaredMethod("main",String[].class).getAnnotations();
    for (int i=0;i<annos.length;i++) {
      System.out.println(annos[i]);
    }
  }
}
