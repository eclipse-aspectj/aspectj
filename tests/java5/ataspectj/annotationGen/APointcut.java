import org.aspectj.lang.reflect.*;

public class APointcut {
	
	@org.aspectj.lang.annotation.Pointcut("execution(* *.*(..))")
	void myPointcut() {};
	
	public static void main(String[] args) throws Exception {
		AjType myType = AjTypeSystem.getAjType(APointcut.class);
		Pointcut pc = myType.getDeclaredPointcut("myPointcut");
	}
	
}