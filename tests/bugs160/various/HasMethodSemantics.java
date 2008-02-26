
public class HasMethodSemantics {
	public static void main(String []argv) {
		System.out.println("Implements Marker? "+(new HasMethodSemantics() instanceof Marker?"yes":"no"));
	}
}

interface Marker {}

aspect X {
	declare parents: HasMethodSemantics && hasmethod(String toString(..)) implements Marker;
}