import org.aspectj.testing.Tester;

/**
 * @author wes
 */
public class IfPCDExprAssignUnparseFailure {
    public static void main(String[] args) {
        Tester.check(true, "compiler test");
    }
}

/**
 * NPE unwinding assignment in if pcd expression:
 * <pre>
 * unexpected exception: 
 *    org.aspectj.compiler.base.InternalCompilerError
 * java.lang.NullPointerException
 *  at org.aspectj.compiler.base.ast.AssignExpr.unparse(AssignExpr.java:78)
 * <pre>
 * in revision 1.26 of AssignExpr.java.
 */
aspect AspectFor {
	static int i;
	pointcut namedIf () 
		: if(0 == (i = 2)) ; // NPE unwinding assignment in if

}
