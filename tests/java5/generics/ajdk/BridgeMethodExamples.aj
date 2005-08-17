public aspect BridgeMethodExamples {
	
	declare warning : execution(Object SubGeneric.foo(Object)) : "no match";
	declare warning : execution(Object Generic.foo(Object)) : "double match";
	declare warning : call(Object SubGeneric.foo(Object)) : "match";
	
	void foo() {
		SubGeneric rawType = new SubGeneric();
        rawType.foo("hi");  // call to bridge method (will result in a runtime failure in this case)
        Object n = new Integer(5);
        rawType.foo(n);     // call to bridge method that would succeed at runtime
	}
}

class Generic<T> {
	
	public T foo(T someObject) {
    	return someObject;
    }

}

class SubGeneric<N extends Number> extends Generic<N> {

    public N foo(N someNumber) {
      return someNumber;
    }

}