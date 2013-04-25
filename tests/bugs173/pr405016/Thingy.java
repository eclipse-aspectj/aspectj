import java.io.Serializable;

@Gimme({Cloneable.class,java.io.Serializable.class})
public class Thingy {
  public static void main(String[] argv) {
    System.out.println("I am serializable? "+(new Thingy() instanceof Serializable));
  }
}

aspect X {
	//declare parents: Thingy implements Serializable;
	declare parents: (@Gimme(value = {Cloneable.class, Serializable.class}) *) implements Serializable;
}
