import org.aspectj.testing.Tester;

public class NotAndPointcut {
    static String str = "";

    public static void main(String args[]){
        new NotAndPointcut().showBug();
	Tester.checkEqual(str, "ran", "advice didn't run");
    }

    public void showBug(){
    }
}

aspect BugInPCD {
    pointcut p(): execution(* NotAndPointcut.showBug(..)) && 
	! ( target(NotAndPointcut) && call(* NotAndPointcut.*(..)));

    before(): p(){
	NotAndPointcut.str += "ran";
    }
}
