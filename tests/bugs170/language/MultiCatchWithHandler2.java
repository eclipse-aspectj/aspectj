public class MultiCatchWithHandler2 {

	public static void main(String[] args) {
		try {
			foo("ta");
		} catch (ExceptionA | ExceptionB ex) {
			bar(ex);
		}
	}

	public static void bar(Exception ea) {

	}

	public static void foo(String s) throws ExceptionA, ExceptionB {
		if (s.equals("ta")) {
			throw new ExceptionA();
		} else {
			throw new ExceptionB();
		}
	}
}

@SuppressWarnings("serial")
class ExceptionA extends Exception {
}

@SuppressWarnings("serial")
class ExceptionB extends Exception {
}


aspect X {
  before(ExceptionA ea): handler(ExceptionA) && args(ea) {
    System.out.println("advice");
  }
}
