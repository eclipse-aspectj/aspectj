import org.aspectj.lang.annotation.*;

public aspect SuppressionDuringMatching {
	
//	// XLint:unmatchedSuperTypeInCall
//	// XLint:adviceDidNotApply
//	before() : call(* Sub.foo()) {
//		
//	}
//	
//	@SuppressAjWarnings
//	before() : call(* Sub.foo()) {
//		
//	}
//	
//	// XLint:unmatchedSuperTypeInCall
//	@SuppressAjWarnings("adviceDidNotApply")
//	before() : call(* Sub.foo()) {
//		
//	}
//	
	
	// XLint:adviceDidNotApply
	@SuppressAjWarnings("unmatchedSuperTypeInCall")
	before() : call(* Sub.foo()) {
		
	}
	
	
}

class Super {
	
	public void foo() {}
	
	void bar() {
		foo();
	}
}

class Sub extends Super {
	
	void bar() {
		foo();
	}
}