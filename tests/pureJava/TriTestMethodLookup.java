import org.aspectj.testing.Tester;

class TriTestMethodLookup {
    static String foo(byte b)  { return "byte"; }
    static String foo(short s) { return "short"; }
    
    static final int byteAsInt = 127;
    static final int shortAsInt = 128;

    static byte byteType = 0;
    static short shortType = 0;
    static boolean notTrue = false;

    public static void main(String[] args) {

	Tester.checkEqual(foo(notTrue ? byteType : byteAsInt),  "byte",  "lub(byte, 127) --> byte");
	Tester.checkEqual(foo(notTrue ? shortType : shortAsInt),"short", "lub(short, 128) --> short");
    }
}

