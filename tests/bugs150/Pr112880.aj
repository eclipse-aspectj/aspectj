
abstract class AbstractAttributeGuiFactory<A,B> {
	
	public A getThis() {
		return null;
	}
	
	public B getThat() {
		return null;
	}
	
}

class ColorAttributeGuiFactory extends AbstractAttributeGuiFactory<C1,C2> {}

class C1 {}

class C2 {}

aspect ForceParameterization {
	
	
	before() : call(C1 *(..)) || call(C2 *(..)) {
		System.out.println("method returning C1 or C2");
	}
	
}

public class Pr112880 {
	
	public static void main(String[] args) {
		ColorAttributeGuiFactory f = new ColorAttributeGuiFactory();
		f.getThis();
		f.getThat();
	}
	
}