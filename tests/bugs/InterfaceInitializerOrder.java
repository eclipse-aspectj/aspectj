import org.aspectj.testing.Tester;

public class InterfaceInitializerOrder {
	public static void main(String[] args) {
	  Base base = new Base();
	  Tester.checkEqual(InitAspect.inits.toString(), "Super1,Super2,SuperInterface,Base,");
	}
}

class Super1 {}

class Super2 extends Super1 {}

interface SuperInterface {}

class Base extends Super2 implements SuperInterface { }

aspect InitAspect {
	public static StringBuffer inits = new StringBuffer();
	
  pointcut outerMatch() : initialization(new(..)) && !within(InitAspect);
  before() : outerMatch() {
  	inits.append(thisJoinPoint.getSignature().getDeclaringType().getName());
  	inits.append(",");
  }
}
