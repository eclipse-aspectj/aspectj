// Four.m() is ITD from another aspect - and itd is annotated
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface A {}

public class Four {
//  public void m() {} --> gone to aspect
  public static void main(String []argv) {
   if (!(new Four() instanceof java.io.Serializable)) System.err.println("declare parents failed");
  }
}

aspect Y {
  public void Four.m() {}
}
aspect X {
  declare parents: hasmethod(@A * * *(..)) implements java.io.Serializable;
}
