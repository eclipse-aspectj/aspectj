public class TwoAnonymous {
    /**/
    Runnable i = new Runnable() {
	    public void run() {
		System.out.println("i");
	    }
	    private Object foo() { return null; }
	};
    Runnable j = new Runnable() {
	    public void run() { 
		System.out.println(new Integer(0));
	    }
	};
    /**/
    public static void main(String[] args) {
	Runnable k = new Runnable() {
                int x = 0;
		public void run() {
		    System.out.println("k");
                    x = 4;
	    }
		private Object foo() { return null; }
	    };

        Runnable k1 = new Runnable() { public void run() { } };

	k.run();
		
    }
}

aspect A {

    before(Runnable r): call(void Runnable.run()) && target(r) {
	System.out.println("calling run: " + r + ", " + thisJoinPoint.getArgs() +", " + thisJoinPoint.getTarget());
    }

    after() returning(Runnable r): call(Runnable+.new()) {
	System.out.println("new runnable: " + r);
    }

    before(): set(int x) {
        System.out.println("setting x");
    }
}
