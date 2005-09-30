import org.aspectj.lang.reflect.*;

public aspect ReferencePointcuts {
	
	pointcut pc1() : call(* *.*(..));
	
	pointcut pc2() : pc1() || execution(* *.*(..));
	
	public static void main(String[] args) throws NoSuchPointcutException {
		AjType myType = AjTypeSystem.getAjType(ReferencePointcuts.class);
		Pointcut p1 = myType.getDeclaredPointcut("pc1");
		if (!p1.getPointcutExpression().toString().equals("call(* *(..))")) 
			throw new RuntimeException("unexpected pc expression: " + p1.getPointcutExpression());
		Pointcut p2 = myType.getDeclaredPointcut("pc2");
		if (!p2.getPointcutExpression().toString().equals("(pc1() || execution(* *(..)))")) 
			throw new RuntimeException("unexpected pc expression: " + p2.getPointcutExpression());

	}
	
}