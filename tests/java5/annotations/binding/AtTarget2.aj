import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

@Retention(RetentionPolicy.RUNTIME)
@interface Material { String material(); }

@Colored(color="yellow") @Material(material="wood")
public class AtTarget2 {
  public static void main(String[]argv) {
    new AtTarget2().m();
    new SubA().m();
    new SubB().m();
  }

  @Colored(color="red")
  public void m() {
    System.err.println("method running\n");
  }
}

@Material(material="metal") @Colored(color="green")
class SubA extends AtTarget2 { }

@Material(material="jelly") @Colored(color="magenta")
class SubB extends SubA { }

aspect X {
  int adviceexecutions = 0;

  before(Colored c,Material m): call(* *(..)) && !within(X) && @target(c) && @target(m) {
    System.err.println("advice running ("+c.color()+","+m.material()+")");
    adviceexecutions++;
    
    if (adviceexecutions == 1) {
      if (!c.color().equals("yellow"))
        throw new RuntimeException("First advice execution, color should be yellow");
      if (!m.material().equals("wood"))
        throw new RuntimeException("First advice execution, material should be wood");
    }
    if (adviceexecutions == 2) {
      if (!c.color().equals("green"))
        throw new RuntimeException("Second advice execution, color should be green");
      if (!m.material().equals("metal"))
        throw new RuntimeException("Second advice execution, material should be metal");
    }
    if (adviceexecutions == 3) {
      if (!c.color().equals("magenta"))
        throw new RuntimeException("Third advice execution, color should be magenta");
      if (!m.material().equals("jelly"))
        throw new RuntimeException("Third advice execution, material should be jelly");
    }
    if (adviceexecutions > 3) 
      throw new RuntimeException("Advice should only run twice");
  }
}

