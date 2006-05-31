import java.sql.SQLException;

public aspect MethodExecution {

	pointcut pc() : execution(public * C.shouldntThrow(..));
	
	Object around() throws SQLException :pc(){
		throw new SQLException(); 
	}
	
	pointcut pc2() : execution(public * C.needsToThrow(..));
	
	// C.needsToThrow still needs to throw the exception because
	// this advice isn't doing anything with exceptions
	before() : pc2() {
	}
	
}

class C {
	
	// don't want the "declared exception is not actually
	// thrown" message because around advice is affecting
	// this method
	public void shouldntThrow() throws SQLException {
	}

	// do want the "declared exception is not actually
	// thrown" message to appear for this method
	public void needsToThrow() throws SQLException{
	}
	
}
