public aspect PerThisWithReference perthis(mypc()) {


  pointcut mypc() : SomeOtherType.pc();


}