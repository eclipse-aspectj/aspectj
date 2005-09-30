import org.aspectj.lang.reflect.*;

public aspect PointcutsWithParams {
	
	pointcut pc1(String s) : args(s);
	
	pointcut pc2(Integer i, Double d, String s) : args(i,d,s);
	
	public static void main(String[] args) throws NoSuchPointcutException {
		AjType myType = AjTypeSystem.getAjType(PointcutsWithParams.class);
		Pointcut p1 = myType.getDeclaredPointcut("pc1");
		AjType<?>[] params = p1.getParameterTypes();
		if (params.length != 1) throw new RuntimeException("expecting one param");
		if (!params[0].getJavaClass().equals(String.class)) throw new RuntimeException("expecting a String");
		String[] names = p1.getParameterNames();
		if (names.length != 1) throw new RuntimeException("expecting one name");
		if (!names[0].equals("s")) throw new RuntimeException("expecting 's', found " + names[0]);
		Pointcut p2 = myType.getDeclaredPointcut("pc2");
		params = p2.getParameterTypes();
		if (params.length != 3) throw new RuntimeException("expecting three params");
		if (!params[0].getJavaClass().equals(Integer.class)) throw new RuntimeException("expecting an Integer");
		if (!params[1].getJavaClass().equals(Double.class)) throw new RuntimeException("expecting a Double");
		if (!params[2].getJavaClass().equals(String.class)) throw new RuntimeException("expecting a String");
		names = p2.getParameterNames();
		if (names.length != 3) throw new RuntimeException("expecting one name");
		if (!names[0].equals("i")) throw new RuntimeException("expecting 'i', found '" + names[0] + "'");
		if (!names[1].equals("d")) throw new RuntimeException("expecting 'd', found '" + names[1] + "'");
		if (!names[2].equals("s")) throw new RuntimeException("expecting 's', found '" + names[2] + "'");
	}
	
}