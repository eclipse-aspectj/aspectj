import org.aspectj.testing.Tester;

public class StringFold {
    static public void main(String[] args) {
	Tester.checkEq("th" + "ere", "the" + "re", "\"th\" + \"ere\" == \"the\" + \"re\"");
	Tester.checkEq(StringFoldHelper.xx, "th", "StringFoldHelper.xx == \"th\"");
	Tester.checkEq(StringFoldHelper.xx + "ere", "there", "StringFoldHelper.xx + \"ere\" == \"there\"");	
	Tester.checkEq(StringFoldHelper.xx + "", StringFoldHelper.xx, "StringFoldHelper.xx + \"\" == StringFoldHelper.xx");

        Tester.checkEq("\477", "'7", "2-digit octal escape");
    }
}

class StringFoldHelper {
    static public final String xx = "th";
}
