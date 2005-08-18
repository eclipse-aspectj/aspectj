
public aspect pr107059_2 {
	
	before() : target(@Foo *) {   // not allowed type pattern in target of course!
		System.out.println("hi");		
	}
	
	void bar(Object o) {
		o.toString();     // generates o hasAnnotation(Foo.class) test
	}
	
}

@interface Foo {}