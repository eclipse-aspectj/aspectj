import org.aspectj.testing.Tester;

class C {
    public void a() {
	T.add("a");
    }
}

aspect A1 { declare precedence: A1, A2;
    pointcut cut(): target(C) && execution(void a());

    before(): A1.cut() { T.add("A1"); }
}

aspect A2 { declare precedence: A2, A3;
    before(): A1.cut() { T.add("A2"); }
}

aspect A3 { declare precedence: A3, A1;
    before(): A1.cut() { T.add("A3"); }
}


class T {
    private static StringBuffer order = new StringBuffer();
    public static void add(String s) { order.append(s); order.append(':'); }
    public static void reset() { order = new StringBuffer(); }

    public static void checkAndReset(String expectedValue) {
	Tester.checkEqual(order.toString(), expectedValue);
	order.setLength(0);
    }
}
