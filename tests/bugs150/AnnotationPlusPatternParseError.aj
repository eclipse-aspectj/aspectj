import java.lang.annotation.*;

public aspect AnnotationPlusPatternParseError {
	 
	pointcut bar() : call(* (@MemberOfMonitoredSet *)+.*(..));
	
	declare warning : bar() : "humbug";
	
}

@interface MemberOfMonitoredSet {}

@MemberOfMonitoredSet
interface I {}

class C implements I {
	
	void bar() {
		foo();
	}
	
	public void foo() {};
	
}