import org.aspectj.weaver.tools.*;

public class ReflectOnAjcCompiledPointcuts {
	
	public static void main(String[] args) {
		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(ReflectOnAjcCompiledPointcuts.class.getClassLoader());
//		PointcutParser p = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();
		PointcutExpression pe = null;
//		pe = p.parsePointcutExpression("PointcutLibrary.propertyAccess()");
//		pe = p.parsePointcutExpression("PointcutLibrary.propertyUpdate()");
//		pe = p.parsePointcutExpression("PointcutLibrary.methodExecution()");
//		pe = p.parsePointcutExpression("PointcutLibrary.propertyGet()");
//		pe = p.parsePointcutExpression("PointcutLibrary.propertySet(Object)");
		pe = p.parsePointcutExpression("PointcutLibrary.getAndThis(Object)");
		pe = p.parsePointcutExpression("PointcutLibrary.getAndTarget(Object)");
		pe = p.parsePointcutExpression("PointcutLibrary.getAndAtAnnotation(MyAnn)");
		pe = p.parsePointcutExpression("PointcutLibrary.getAndAtWithin(MyAnn)");
		pe = p.parsePointcutExpression("PointcutLibrary.getAndAtWithinCode(MyAnn)");
		pe = p.parsePointcutExpression("PointcutLibrary.getAndAtThis(MyAnn)");
		pe = p.parsePointcutExpression("PointcutLibrary.getAndAtTarget(MyAnn)");
		pe = p.parsePointcutExpression("PointcutLibrary.setAndAtArgs(MyAnn)");
		
		
		PointcutParameter pp = p.createPointcutParameter("foo",Object.class);
		p.parsePointcutExpression("execution(* *(..)) && PointcutLibrary.propertySet(foo)",
									Object.class,
									new PointcutParameter[] {pp});
		 
	}
	
	
}