import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareMixin;

@Aspect
public class PR298388 {

	@DeclareMixin("Thing2")
	public static <T> Thing<T> createThingImplementation() {
		return new ThingImpl<T>();
	}
	
	public static void main(String[] args) {
		Thing<String> ts = (Thing<String>) new Thing2<String>();
		ts.wibble();
		ts.wibble("abc");
		String s = ts.wibbleBack("wobble");
		System.out.println("done");
	}
	
}

class Thing2<X> {
}

interface Thing<X> {
	void wibble();
	void wibble(X x);
	X wibbleBack(X x);
}

class ThingImpl<X> implements Thing<X> {
	ThingImpl() {
	}

	public void wibble() {
	}
	
	public void wibble(X x) {}
	
	public X wibbleBack(X x) { return x;}

}
