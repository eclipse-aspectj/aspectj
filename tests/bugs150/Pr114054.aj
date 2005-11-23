public class Pr114054 {
	public static boolean passed;
	public static void main(String[] args) {
		SampleSeries me = new SampleSeries();
		me.okSeries();
		me.open();
		me.close();
		if (!passed) {
			throw new Error("failed to advise...");
		}
	}
	static class SampleSeries {
		void open() {}
		void close() {}
		void okSeries() {open(); close();}
	}
	static aspect AAAA 
	// comment this out, and !call(...) works
	pertarget(tracked())
	{
	    protected final pointcut tracked() : 
			call(void SampleSeries.*()) 
			// comment this out, and pertarget works...
			&& !call(void SampleSeries.*Series())
			;
		before() : tracked() {
			Pr114054.passed = true;
		}		
	}
}