import java.util.*;

public class Pr109283 {
	
	enum Foo { Wibble, Wobble, Woo }
	
	public static void main(String[] args) throws Exception {
		EnumSet<Foo> set = EnumSet.noneOf(Foo.class);
		C c2 = Recipient.instanceOf(C.class);
	}
	
	
}

class C {}

class Recipient {}

aspect Donor {
	
	static <E> E Recipient.first(List<E> elements) { return elements.get(0); }
	
	public static <T> T Recipient.instanceOf(Class<T> aT) throws Exception { 
		return aT.newInstance(); 
	}
	
}
