public class Test{
  public static void main(String[] arguments){
	Test test = new Test();
	test.sayHello();
	test.doSayHello();
  }

  public void sayHello(){
	doSayHello();
  }

  public void doSayHello(){
	System.out.println("hello.");
  }
}
