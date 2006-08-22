import java.util.List;

aspect A {
	
	pointcut p() : execution(* *.*(..)) || execution(*.new(..));
	
	before() : p() {
		
	}
	
}

class C {
	
	public void genericMethod(List<String> l) {}
	public void genericMethod2(MyGenericClass<String,MyClass> m) {}

}

class MyClass {}

class MyGenericClass<X,Y> {}
