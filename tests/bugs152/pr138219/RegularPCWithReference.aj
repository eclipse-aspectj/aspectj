public aspect RegularPCWithReference {

  pointcut refersToMypc() : mypc();

  pointcut mypc() : SomeOtherType.pc();


}