public aspect TestAroundAspect{
  void around(): execution(void Test.sayHello(String)){
    proceed();
  }
}

