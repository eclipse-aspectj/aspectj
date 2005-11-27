import org.aspectj.weaver.tools.*;
import java.lang.reflect.*;

public class RuntimePointcuts {
	
	
	public static void main(String[] args) throws Exception {
		PointcutParser parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(RuntimePointcuts.class.getClassLoader());
//		parser.setClassLoader(RuntimePointcuts.class.getClassLoader());
		PointcutExpression pc1 = parser.parsePointcutExpression("PCLib.anyMethodExecution()");
		PointcutParameter param = parser.createPointcutParameter("s",String.class);
		PointcutExpression pc2 = parser.parsePointcutExpression("PCLib.joinPointWithStringArg(s)",RuntimePointcuts.class,new PointcutParameter[] {param});
		Method foo = RuntimePointcuts.class.getDeclaredMethod("foo", new Class[0]);
		Method bar = RuntimePointcuts.class.getDeclaredMethod("bar",new Class[] {String.class});
		ShadowMatch fooMatch1 = pc1.matchesMethodExecution(foo);
		if (!fooMatch1.alwaysMatches()) throw new RuntimeException("fooMatch1 should always match");
		ShadowMatch fooMatch2 = pc2.matchesMethodExecution(foo);
		if (!fooMatch2.neverMatches()) throw new RuntimeException("fooMatch2 should never match");
		ShadowMatch barMatch1 = pc1.matchesMethodExecution(bar);
		if (!barMatch1.alwaysMatches()) throw new RuntimeException("barMatch1 should always match");
		ShadowMatch barMatch2 = pc2.matchesMethodExecution(bar);
		if (!barMatch2.alwaysMatches()) throw new RuntimeException("barMatch2 should always match");
		JoinPointMatch jpm = barMatch2.matchesJoinPoint(new Object(),new Object(),new Object[] {"hello"});
		if (!jpm.matches()) throw new RuntimeException("should match at join point");
		if (!jpm.getParameterBindings()[0].getBinding().toString().equals("hello"))
			throw new RuntimeException("expecting s to be bound to hello");		
	}
	
	public void foo() {}
	
	public void bar(String s) {}
	
}