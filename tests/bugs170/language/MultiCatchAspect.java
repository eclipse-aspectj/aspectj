public class MultiCatchAspect {

	public static void main(String[] args) {
        }
}

aspect X {

	before(): execution(* main(..)) {
		try {
			foo("abc");
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
