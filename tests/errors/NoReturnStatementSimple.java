
/** error falls to javac to detect - confirm detected in ajc */
public class NoReturnStatementSimple {
	static String noReturn() { } // compile error here detected by javac
    public static void main(String[] args) { }
}
