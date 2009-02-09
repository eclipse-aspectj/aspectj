import java.util.*;

interface I {
	public <T> List<T> getStuff();
}

class C<D extends Set> implements I {
	public <T extends D> List<T> getStuff(){
		return new LinkedList<T>();
	}
}

aspect X {
	public <T> List<T> I.getStuff(){
		return new ArrayList<T>();
	}
}
