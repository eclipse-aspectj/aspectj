public aspect TestAspect extends AbstractAspect{
  public pointcut directCall():
    execution(void Test.doSayHello(..))
    && cflow(execution(void Test.sayHello(..)))
  ;
  
  public pointcut badCall():
  	call(void Test.doSayHello(..)) && withincode(void Test.sayHello(..));

  void noteDirectCall() {
    sawDirectCall = true;
  }
  
  public static boolean sawDirectCall = false;
}
