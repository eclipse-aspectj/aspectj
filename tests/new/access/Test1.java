import org.aspectj.testing.Tester;

import pc.C;
import psub.SubC;

public class Test1 {
    public static void main(String[] args) {
	C c = new C();
	SubC subc = new SubC();

	Tester.checkEqual(c.getInternalPackage(), "pc");
	Tester.checkEqual(subc.getInternalPackage(), "pc");
	Tester.checkEqual(subc.getRealPackage(), "psub");

    }
}
