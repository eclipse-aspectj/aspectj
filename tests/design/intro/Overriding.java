import org.aspectj.testing.Tester;

public class Overriding {
    public static void main(String[] args) {
	SuperC sc = new C();

	Tester.checkEqual(sc.m(), "A2");
	Tester.checkEqual(A3.getMi(sc), "A3.I2");
	Tester.checkEqual(A4.getMi(sc), "A4.I2");
	Tester.checkEqual(A3.getM2(sc), "A3.Inner");
	Tester.checkEqual(A4.getM2(sc), "A4.Inner");
    }
}

abstract class SuperC { }
class C extends SuperC {}
class SubC extends C {}

aspect A1 {
    abstract String SuperC.m();
}


aspect A2 {
    String C.m() {
	return "A2";
    }
}

class A3 {
    static aspect I1 {
	private abstract String SuperC.mi();
    }

    static aspect I2 {
	private String C.mi() {
	    return "A3.I2";
	}
    }

    public static String getMi(SuperC sc) {
	return sc.mi();
    }


    static aspect Inner {
	private abstract String SuperC.m2();
	private String C.m2() {
	    return "A3.Inner";
	}
    }
    public static String getM2(SuperC sc) {
	return sc.m2();
    }
}

class A4 {
    static aspect I1 {
	private abstract String SuperC.mi();
    }

    static aspect I2 {
	private String C.mi() {
	    return "A4.I2";
	}
    }

    public static String getMi(SuperC sc) {
	return sc.mi();
    }

    static aspect Inner {
	private abstract String SuperC.m2();
	private String C.m2() {
	    return "A4.Inner";
	}
    }
    public static String getM2(SuperC sc) {
	return sc.m2();
    }

}
