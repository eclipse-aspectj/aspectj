package test;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

interface A {
	void doA();
}

interface BBad extends A {
	void doB();
}

interface BGood extends A {
	void doB();
	void doA();
}

class TargetBad { }
class TargetGood { }

@Aspect
 class DeclareParentsAspect {

//	@DeclareParents(value = "test.TargetGood", defaultImpl = BImplGood.class)
 //       private BGood bGood;

	@DeclareParents(value = "test.TargetBad", defaultImpl = BImplGood.class)
	private BBad bBad;

	public static class BImplGood implements BGood, BBad {

		public void doB() {
			System.out.println("doB");
		}

		public void doA() {
			System.out.println("doA");
		}
	}
}

public class Code {
	public static void main(String... args) { 
/*
		{
			TargetGood target = new TargetGood();
			BGood b = (BGood) target;
			b.doB();
			b.doA();
		}
*/
		{
			TargetBad target = new TargetBad();
			BBad b = (BBad) target;
			b.doB();

			/*
			The following line is the problem.
			The Generated class should refer to ajc$test_DeclareParentsAspect$test_BBad

			Instead...

			Exception in thread "main" java.lang.NoSuchFieldError: ajc$test_DeclareParentsAspect$test_A
			at test.TargetBad.doA(TargetBad.java:1)
			at test.Main.main(Main.java:21)
			*/
			b.doA();
		}
	}

}
