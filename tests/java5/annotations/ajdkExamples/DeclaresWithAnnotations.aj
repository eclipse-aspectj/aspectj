import org.xyz.model.Model;

public aspect DeclaresWithAnnotations {
	
	declare warning : withincode(@PerformanceCritical * *(..)) &&
      call(@ExpensiveOperation * *(..))
      : "Expensive operation called from within performance critical section";
	
	declare error : call(* org.xyz.model.*.*(..)) &&
       !@within(Trusted)
       : "Untrusted code should not call the model classes directly";
	
}

@interface PerformanceCritical {}

@interface ExpensiveOperation {}

@interface Trusted {}


class Foo {
	
	@PerformanceCritical Foo getFoo() {
		Model m = new Model();  
		m.foo();              // DE
		Foo foo = makeFoo();  // DW
		return foo;
	}
	
	Foo getFoo2() {
		Foo foo = makeFoo();
		return foo;
	}
	
	@ExpensiveOperation Foo makeFoo() {
		return new Foo();
	}
}

@Trusted class Goo {
	
	public void goo() {
		Model m = new Model();
		m.foo();
	}
	
}