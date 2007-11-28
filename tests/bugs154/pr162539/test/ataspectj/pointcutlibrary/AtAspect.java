package test.ataspectj.pointcutlibrary;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AtAspect {
//	@Pointcut("execution(public void main(String[]))")
//	public void mainMethod () {
//	}

	@Before("(PointcutLibrary.mainMethod())")
	public void beforeMainMethod (JoinPoint.StaticPart thisJoinPointStaticPart, JoinPoint thisJoinPoint) {
		System.out.println("AtAspect.beforeMainMethod() " + thisJoinPoint);
	}

}
