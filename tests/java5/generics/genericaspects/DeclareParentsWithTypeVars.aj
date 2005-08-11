import java.lang.reflect.*;

abstract aspect GiveMeFoo<T> {
	
	declare parents : C implements I<T>;
	
}

public aspect DeclareParentsWithTypeVars extends GiveMeFoo<String> {
	
	public static void main(String[] args) {
		C c = new C();
		if (! (c instanceof I)) throw new RuntimeException("C should implement I");
		Type[] superinterfaces = C.class.getGenericInterfaces();
		if (! (superinterfaces[0] instanceof ParameterizedType)) throw new RuntimeException("Expected to get parameterized interface but found " + superinterfaces[0]);
		ParameterizedType pt = (ParameterizedType) superinterfaces[0];
		Type[] typeArguments = pt.getActualTypeArguments();
		if (typeArguments[0] != String.class) throw new RuntimeException("Expecting String parameter but found " + typeArguments[0]);
	}
	
}

class C {
	
//	public Object identity(Object o) { return o; }
	
	public String identity(String aString) {
		return aString;
	}
	
}

interface I<E> {
	
	E identity(E anE);
	
}
