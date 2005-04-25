import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;

public abstract aspect PointcutModifiers {
	
	protected abstract pointcut p1();
	
	pointcut p2();
	
	public pointcut p3(): get(String s);
	
	protected pointcut p4() : set(String s);
	
	private pointcut p5() : call(* String.*(..));
	
	public static void main(String[] args) throws NoSuchPointcutException {
		AjType myType = AjTypeSystem.getAjType(PointcutModifiers.class);
		assertTrue(myType != null, "found my type");
		Pointcut[] pointcuts = myType.getDeclaredPointcuts();
		assertTrue(pointcuts != null, "found some pointcuts");
		if (pointcuts.length != 5) throw new RuntimeException("Expecting 5 pointcuts");
		Pointcut pc1 = myType.getDeclaredPointcut("p1");
		assertTrue(pc1 != null, "found pc1");
		Pointcut pc2 = myType.getDeclaredPointcut("p2");
		assertTrue(pc2 != null, "found pc2");
		Pointcut pc3 = myType.getDeclaredPointcut("p3");
		assertTrue(pc3 != null, "found pc3");
		Pointcut pc4 = myType.getDeclaredPointcut("p4");
		assertTrue(pc4 != null, "found pc4");
		Pointcut pc5 = myType.getDeclaredPointcut("p5");
		assertTrue(pc5 != null, "found pc5");
		assertTrue(Modifier.isAbstract(pc1.getModifiers()),"pc1 is abstract" );
		assertTrue(Modifier.isProtected(pc1.getModifiers()),"pc1 is protected");
		assertTrue(!Modifier.isPrivate(pc2.getModifiers()),"pc2 not private");
		assertTrue(!Modifier.isPublic(pc2.getModifiers()),"pc2 not public");
		assertTrue(!Modifier.isProtected(pc2.getModifiers()),"pc2 not protected");
		assertTrue(Modifier.isPublic(pc3.getModifiers()),"pc3 is public");
		assertTrue(Modifier.isProtected(pc4.getModifiers()),"pc1 is protected");
		assertTrue(Modifier.isPrivate(pc5.getModifiers()),"pc5 is private");
	}
	
	private static void assertTrue(boolean expr, String msg) {
		if (!expr) throw new RuntimeException(msg);
	}
}