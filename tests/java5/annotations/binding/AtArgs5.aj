import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Colored   { String color(); }
@Retention(RetentionPolicy.RUNTIME) @interface Fruit     { String value(); }

@Colored(color="yellow") @Fruit("banana") class YB {}
@Colored(color="green") @Fruit("apple") class GA {}
@Colored(color="red") @Fruit("tomato") class RT {}

public class AtArgs5 {
  public static void main(String[]argv) {
    m(new YB(),new GA(),new RT());
  }

  public static void m(Object a,Object b,Object c) { }

}

aspect X {
  static int count = 0;

  before(Colored c1,Fruit f,Colored c2): execution(* m(..)) && !within(X) && @args(c1,f,c2) {
    System.err.println("Two colors:"+c1.color()+","+c2.color());
    System.err.println("Fruit is:"+f.value());
    count++;
    if (!c1.color().equals("yellow")) 
      throw new RuntimeException("Color1 should be yellow");
    if (!f.value().equals("apple")) 
      throw new RuntimeException("Fruit should be apple");
    if (!c2.color().equals("red")) 
      throw new RuntimeException("Color2 should be red");
    
  }

   public static void verify() {
    if (count!=3) throw new Error("Should be 3 runs: "+count);
   }
}

