import org.aspectj.compiler.base.ast.*;



aspect Wins {

	pointcut showError(ASTObject ast, String msg): target(ast) && call(void showError(msg));

	void around(ASTObject ast, String msg): showError(ast, msg) {
		System.out.println("hi");
		proceed(ast, msg);
	}
}

aspect Loses {
	
	pointcut showError(ASTObject ast, String msg): 
            within(org.aspectj.compiler..*) && target(ast) && call(void AST.showError(msg));
	
	void around(ASTObject ast, String msg): showError(ast, msg) {
		System.out.println("hi");
		proceed(ast, msg);
	}
}

