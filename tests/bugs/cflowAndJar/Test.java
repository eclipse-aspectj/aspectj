import org.aspectj.testing.Tester;

public class Test {
  public static void main(String[] arguments){
	Test test = new Test();
	Tester.checkEqual(TestAspect.sawDirectCall, false, "no calls");
	
	test.doSayHello();
	Tester.checkEqual(TestAspect.sawDirectCall, false, "non-cflow");
	
	test.sayHello();
	Tester.checkEqual(TestAspect.sawDirectCall, true, "in-cflow");
  }

  public void sayHello(){
	doSayHello();
  }

  public void doSayHello(){
	System.out.println("hello.");
  }
}
