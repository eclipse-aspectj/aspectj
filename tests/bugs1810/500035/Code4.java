import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

public aspect Code4 {
 
    void around(Foo targeto, String s): call(* Foo.run(String)) && args(s) && target(targeto) {
        System.out.println("first: binding target, just passing everything through");
        proceed(targeto, s);
    }

    public static void main(String []argv) {
      new Foo(0).execute();
    }
}

class Foo {
  int i;
  public Foo(int i) {
    this.i = i;
  }

  public void execute() {
      Foo f1 = new Foo(1);
      Foo f2 = new Foo(2);
      f1.run("abc");
  }

  public void run(String s) {
    System.out.println("Executing run("+s+") on "+this.toString());
  }

  public String toString() {
    return ("Foo(i="+i+")");
  }
}
