
public aspect Aspect {
	//:method-call(void Foo.method1())=real
	//:(virtual) method-call(void Foo.method2())=virtual
	pointcut calls(): call(* Foo.*(..));
	
	//:(virtual) method-call(void Bar.bar())=virtual
	pointcut callBar():call(* Bar.*(..));
	
	//:method-call(void Foo.method1())=real
	//:(virtual) method-call(void Foo.method2())=virtual
	pointcut callsWithin(): call(* Foo.*(..)) && within(Bar);

	//:method-call(void Foo.method1())=real
	//:(virtual) method-call(void Foo.method2())=virtual
	pointcut callsWithincode(): call(* Foo.*(..))&&withincode(* Bar.*(..));

}
