public class PR318 {
    public static void main(String[] args) {
        new PR318().realMain(args);
    }
    public void realMain(String[] args) {
        Bar.bar();
        org.aspectj.testing.Tester.check(caught, "Exception wasn't caught");
    }
    static boolean caught = false;
    
}

class Foo {
    static void foo () throws Exception {
	throw new IllegalArgumentException("foo!");
    }
}

class Bar {
    static void bar () {
	try {
	    Foo.foo();
	} catch (Exception e) {
	}
    }
}

aspect A {
    before (Exception e): handler(Exception) && args(e) {
	if (e instanceof IllegalArgumentException) {
            PR318.caught = true;
	}
    }
}
