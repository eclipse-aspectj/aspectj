import org.aspectj.testing.Tester;

/** @testcase PR#52107 declare int field on interface */
public class IntFieldOnInterface implements Runnable {
    public static final int caseid = 2;
	public static void main(String[] args) {
		Tester.expectEvent("A name=2");
        Tester.expectEvent("R name=2");
        IntFieldOnInterface test
            = new IntFieldOnInterface();
        test.run();
        test.blah();
		Tester.checkAllEvents();
	}
	public void run() {
        switch (name) {
            case (IntFieldOnInterface.caseid) :
                Tester.event("R name=" + name);
                break;
            default :
                throw new Error("bad switch");
        }
	}
}

aspect A {
	public int Runnable.name = IntFieldOnInterface.caseid;
	public void Runnable.blah() {
		switch (name) {
			case (IntFieldOnInterface.caseid) : 
				Tester.event("A name=" + name);
				break;
			default :
				throw new Error("bad switch");
		}
	}
}