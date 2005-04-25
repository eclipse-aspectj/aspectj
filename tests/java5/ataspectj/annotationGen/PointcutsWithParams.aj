import org.aspectj.lang.reflect.*;

public aspect PointcutsWithParams {
	
	pointcut pc1(String s) : args(s);
	
	pointcut pc2(Integer i, Double d, String s) : args(i,d,s);
	
	public static void main(String[] args) throws NoSuchPointcutException {
		AjType myType = AjTypeSystem.getAjType(PointcutsWithParams.class);
		Pointcut p1 = myType.getPointcut("pc1");
		Class[] params = p1.getParameterTypes();
		if (params.length != 1) throw new RuntimeException("expecting one param");
		if (!params[0].equals(String.class)) throw new RuntimeException("expecting a String");
		Pointcut p2 = myType.getPointcut("pc2");
		params = p2.getParameterTypes();
		if (params.length != 3) throw new RuntimeException("expecting three params");
		if (!params[0].equals(Integer.class)) throw new RuntimeException("expecting an Integer");
		if (!params[1].equals(Double.class)) throw new RuntimeException("expecting a Double");
		if (!params[2].equals(String.class)) throw new RuntimeException("expecting a String");
	}
	
}