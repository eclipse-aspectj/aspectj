package p.q;
import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME) @interface Colored {String value();}
@Retention(RetentionPolicy.RUNTIME) @interface Fruit {String value();}
@Retention(RetentionPolicy.RUNTIME) @interface Material {String value();}

aspect AllTogether {

  declare @type: DeathByAnnotations: @Colored("red");
  declare @method: * m*(..): @Fruit("tomato");
  declare @constructor: DeathByAnnotations.new(..): @Fruit("tomato");

  declare @field: * DeathByAnnotations.*: @Material("wood");

}

public class DeathByAnnotations {
  int i;
  static String s;

  public static void main(String[]argv) {
    new DeathByAnnotations().i = 3;
    s = "hello";
    new DeathByAnnotations().m1();
    new DeathByAnnotations(3).m2();
  }

  public DeathByAnnotations() {}
  public DeathByAnnotations(int i) {}

  public void m1() {}
  public void m2() {}

}
