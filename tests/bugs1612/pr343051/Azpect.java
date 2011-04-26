package q;
import p.*;

privileged aspect X {
  public void Code2.Inner.bar() {
    Runnable r = new Runnable() {
      public void run() {
        System.out.println("abc");
      }
    };
    r.run();
  }
}
