

import org.aspectj.testing.Tester;

public aspect DecParents {
	private interface I {
		public abstract String doit();
	}
	
	public String I.doit() {
		return "foo";
	}
	
	declare parents: Main implements I;
	
	before(): execution(void Main.main(..)) {
	}
	
    public static void main(String[] args) {
    	I i = new Main();
    	System.out.println("Main: " + i.doit());
    }
}