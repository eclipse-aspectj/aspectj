public aspect DeclareSoftRuntimeException {
	
	declare soft : MyRuntimeException : execution(* *(..));
	declare soft : MyException : execution(* *(..));
	declare soft : Exception : execution(void throwMyExceptionButNotReally());
	
	public static void main(String[] args) {
		try {
			throwMyRuntimeException();
		} catch(Exception ex) {
			System.out.println(ex.getClass().getName());
		}
		try {
			throwMyException();
		} catch(Exception ex) {
			System.out.println(ex.getClass().getName());
		}
		try {
			throwMyExceptionButNotReally();
		} catch(Exception ex) {
			System.out.println(ex.getClass().getName());
		}
	}
	
	private static void throwMyRuntimeException() {
		throw new MyRuntimeException();
	}
	
	private static void throwMyException() throws MyException {
		throw new MyException();
	}
	
	private static void throwMyExceptionButNotReally() throws MyException {
		throw new MyRuntimeException();
	}
	
}

class MyRuntimeException extends RuntimeException {}

class MyException extends Exception {}
