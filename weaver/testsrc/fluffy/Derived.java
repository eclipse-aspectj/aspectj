package fluffy;

import java.io.IOException;

public class Derived extends Base {

	public static void onlyDerived() throws IOException, CloneNotSupportedException {}
	public static void both() {}
	
	public int onlyDerived;
	public int both;
	
	public Derived() {}
	
	public void m() {}
	
}
