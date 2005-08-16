import java.io.*;

abstract aspect ExceptionHandling<T extends Throwable> {
	
	protected abstract void onException(T anException);
	
	protected abstract pointcut inExceptionHandlingScope();
	
	declare soft: T : inExceptionHandlingScope();
	
	after() throwing (T anException) : inExceptionHandlingScope() {
		onException(anException);
	}
	
}


public aspect DeclareSoftWithTypeVars extends ExceptionHandling<IOException>{
	
	protected pointcut inExceptionHandlingScope() :
		call(* doIO*(..));
	
	protected void onException(IOException ex) {
		System.err.println("handled exception: " + ex.getMessage());
		throw new MyDomainException(ex);
	}
	
	public static void main(String[] args) {
		C c = new C();
		try {
		c.doIO();
		} catch (MyDomainException ex) {
			System.err.println("Successfully converted to domain exception");
		}
	}
	
}

class C {
	
	public void doIO() throws IOException {
		throw new IOException("io, io, it's off to work we go...");
	}
	
}

class MyDomainException extends RuntimeException {
	public MyDomainException(Throwable t) {
		super(t);
	}
}