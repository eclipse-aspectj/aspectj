import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

@Retention(RetentionPolicy.RUNTIME)
@interface Material { String material(); }

@Colored(color="yellow") @Material(material="wood")
public class AtThis2 {
  public static void main(String[]argv) {
    new AtThis2().start();
    new SubA().start();
    new SubB().start();
    X.verify();
  }

  @Colored(color="red")
  public void m() {
    System.err.println("method running\n");
  }
  public void start() { m(); }
}

@Material(material="metal") @Colored(color="green")
class SubA extends AtThis2 { }

@Material(material="jelly") @Colored(color="magenta")
class SubB extends SubA { }

aspect X {
  static int count = 0;

  before(Colored c,Material m): call(* m(..)) && !within(X) && @this(c) && @this(m) {
    System.err.println("advice running ("+c.color()+","+m.material()+")");
    count++;
    
    if (count== 1) {
      if (!c.color().equals("yellow"))
        throw new RuntimeException("First advice execution, color should be yellow:"+c.color());
      if (!m.material().equals("wood"))
        throw new RuntimeException("First advice execution, material should be wood:"+m.material());
    }
    if (count == 2) {
      if (!c.color().equals("green"))
        throw new RuntimeException("Second advice execution, color should be green");
      if (!m.material().equals("metal"))
        throw new RuntimeException("Second advice execution, material should be metal");
    }
    if (count == 3) {
      if (!c.color().equals("magenta"))
        throw new RuntimeException("Third advice execution, color should be magenta");
      if (!m.material().equals("jelly"))
        throw new RuntimeException("Third advice execution, material should be jelly");
    }
  }

 public static void verify() {
    if (count!=3) throw new Error("Should be 3 runs: "+count);
  }
}

