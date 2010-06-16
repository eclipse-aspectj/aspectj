public aspect AspectB {
  before():staticinitialization(!Aspect*) { System.out.println("staticinitialization");}
}
