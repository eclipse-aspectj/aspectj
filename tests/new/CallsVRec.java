import org.aspectj.compiler.base.ast.*;

public class CallsVRec {
    public static void main(String[] args) {
        new CallsVRec().realMain(args);
    }
    public void realMain(String[] args) {
        org.aspectj.testing.Tester.check(true, "Compiled!");
    }
    
    public CallsVRec() {
    }
}




aspect Wins {

	pointcut showError(ASTObject ast, String msg):
            target(ast)
            && call(void ASTObject.showError(String))
            && args(msg);

	void around(ASTObject ast, String msg): showError(ast, msg) {
		System.out.println("hi");
		proceed(ast, msg);
	}
}

aspect Loses {
	
	pointcut showError(ASTObject ast, String msg):
            target(ast)
            && call/*s*/(void ASTObject.showError(String))
            && args(msg);
	
	void around(ASTObject ast, String msg): showError(ast, msg) {
		System.out.println("hi");
		proceed(ast, msg);
	}
}

