import org.aspectj.weaver.tools.*;

public class ReflectOnAjcCompiledPointcuts {
	
	public static void main(String[] args) {
		PointcutParser p = new PointcutParser();
		PointcutExpression pe = null;
		pe = p.parsePointcutExpression("PointcutLibrary.propertyAccess()");
		pe = p.parsePointcutExpression("PointcutLibrary.propertyUpdate()");
		pe = p.parsePointcutExpression("PointcutLibrary.methodExecution()");
		pe = p.parsePointcutExpression("PointcutLibrary.propertyGet()");
		pe = p.parsePointcutExpression("PointcutLibrary.propertySet(Object)");
		
		PointcutParameter pp = p.createPointcutParameter("foo",String.class);
		p.parsePointcutExpression("execution(* *(..)) && PointcutLibrary.propertySet(foo)",
									Object.class,
									new PointcutParameter[] {pp});
		 
	}
	
	
}