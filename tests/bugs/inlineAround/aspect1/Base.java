package aspect1;

public abstract aspect Base {
	private Helper h = new Helper();
	{
	    h.h1 = new Helper();
	    h.h1.h1 = new Helper();
	}

	private class Inner {
	    String data = "inner";
	}
	
	protected abstract pointcut where();
	
	Object around(double d, int i): where() && args(i, d) {
		String s = h.data + h.h1.data + h.h1.h1.data + d + i;
		System.err.println(s);
		return proceed(d, i);
	}
}


class Helper {
	String data = "helper";
	Helper h1;
	String getData() {
		return data;
	}
}