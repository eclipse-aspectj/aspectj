class UndefinedPointCut_PR396 {
    public static void main(String[] args) {
        org.aspectj.testing.Tester.check(false, "Shouldn't have compiled");
    }
}

aspect AspectTest { //of eachJVM() {
  pointcut pc4(): callsto(pc2()); //pc2 is undefined
  before(): pc4() {}
}
