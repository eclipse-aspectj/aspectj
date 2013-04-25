import java.io.Serializable;

@Gimme(Serializable.class)
public class Thingy {
  public static void main(String[] argv) {
    System.out.println("I am serializable? "+(new Thingy() instanceof Serializable));
  }
}

aspect X {
	//declare parents: Thingy implements Serializable;
	declare parents: (@Gimme(value = Serializable.class) *) implements Serializable;
}
