

aspect AspectForIfPCDExprJoinPointVisibleCE {
	// todo: derives from more general binding problems with named pointcuts
	pointcut errorAccessingThisJoinPointStaticPart () 
		: if (thisJoinPointStaticPart != null); // CE: can't bind name thisJoinPointStaticPart 
	pointcut errorAccessingThisJoinPoint () 
		: if (thisJoinPoint != null) && if(thisJoinPoint.getSignature() != null); // CE: can't bind name thisJoinPoint

	before () 
		: within(IfPCDExprJoinPointVisibleCE)
		&& errorAccessingThisJoinPoint() {
		System.err.println("before thisJoinPoint");
	}
	before () 
		: within(IfPCDExprJoinPointVisibleCE)
		&& errorAccessingThisJoinPointStaticPart() {
		System.err.println("before thisJoinPointStaticPart");
	}
}
public class IfPCDExprJoinPointVisibleCE {
	public static void main(String[] args) {
		System.err.println("ok - main running after ");
	}
} 
