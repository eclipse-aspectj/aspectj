import org.aspectj.compiler.base.ast.*;



aspect Wins {

	pointcut showError(ASTObject ast, String msg):
            within(org.aspectj..*) && target(ast) && args(msg) && call(void showError(String));

	void around(ASTObject ast, String msg): showError(ast, msg) {
		System.out.println("hi");
		proceed(ast, msg);
	}
}

aspect Loses {
	
	pointcut showError(ASTObject ast, String msg):
            target(ast) && args(msg) && call(void showError(String));
	
	void around(ASTObject ast, String msg): showError(ast, msg) {
		System.out.println("hi");
		proceed(ast, msg);
	}
}

