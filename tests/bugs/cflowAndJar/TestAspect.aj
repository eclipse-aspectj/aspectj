public aspect TestAspect extends AbstractAspect{
  public pointcut directCall():
    execution(void Test.doSayHello(..))
    && cflow(execution(void Test.direct(..)))
  ;
}
