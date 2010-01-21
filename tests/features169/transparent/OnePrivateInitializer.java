import java.lang.annotation.*;

public class OnePrivateInitializer {
  public static void main(String[] argv) {
    int i = new OnePrivateInitializer().run();
    if (i!=42) throw new RuntimeException(Integer.toString(i));
  }
}

aspect X {
  private int OnePrivateInitializer.x = 42;

  public int OnePrivateInitializer.run() {
    return x;
  }
}
