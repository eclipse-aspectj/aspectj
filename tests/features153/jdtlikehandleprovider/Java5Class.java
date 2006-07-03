package pkg;

import java.util.List;

public class Java5Class {

	public void method2(List l) {
	}
	
	public void genericMethod1(List<String> s) {
	}
	
	public void genericMethod2(List<String> s, MyGenericClass<Integer> m) {
	}
	
	public void genericMethod3(int i, List<String> s) {
	}
	
	public void genericMethod4(MyGenericClass2<String,Integer> m) {}
}

class MyGenericClass<T> {}

class MyGenericClass2<X,Y> {}
