public class NullPointerOnGetsSimple {
    public static void main(String[] args) {
        PrintService ps = new PrintService();
        ps.string = "after";
        org.aspectj.testing.Tester.checkEqual("after", ps.string);
    }
}

class PrintService {
    String string = "before";
}

aspect Aspect {
    pointcut needPrinter(PrintService ps): get(String PrintService.string) && target(ps) && 
        !within(Aspect);
    String around(PrintService ps): needPrinter(ps) {
        System.out.println("around");
        org.aspectj.testing.Tester.checkEqual("after", ps.string);
	return ps.string;
    }
}
