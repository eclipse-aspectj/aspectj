import org.aspectj.testing.Tester;

import java.util.*;

public class InnerAccess {
    public static void main(String[] args) {
	Tester.checkEqual(new C().getCount(), 2);
    }
}


class C {
    protected int i = 2;
    private String s = "hi";

    public int getCount() {
	return new Object() {
		public int m() {
		    return s.length();
		}
	    }.m();
    }
}


class D implements Map.Entry {
}
