import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Colored   { String color(); }
@Retention(RetentionPolicy.RUNTIME) @interface Fruit     { String value(); }

@Colored(color="yellow") @Fruit("banana") class YB {}
@Colored(color="green") @Fruit("apple") class GA {}
@Colored(color="red") @Fruit("tomato") class RT {}

public class AtArgs1 {
  public static void main(String[]argv) {
    m(new YB(),new GA(),new RT());

    X.verify();
  }

  public static void m(Object a,Object b,Object c) { }

}

aspect X {

  static int count = 0;

  before(Colored c1,Colored c2,Colored c3): call(* m(..)) && !within(X) && @args(c1,c2,c3) {
    System.err.println("Colors are "+c1.color()+","+c2.color()+","+c3.color());
    count++;
    if (!c1.color().equals("yellow")) 
      throw new RuntimeException("Color1 should be yellow");
    if (!c2.color().equals("green")) 
      throw new RuntimeException("Color2 should be green");
    if (!c3.color().equals("red")) 
      throw new RuntimeException("Color3 should be red");
    
  }

  public static void verify() {
    if (count!=1) throw new Error("Should be 1 run: "+count);
  }
}

