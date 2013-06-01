// A Java7 aspect we can weave into the Java8 code

aspect SimpleAspect001 {
  before(): within(!SimpleAspect001) {
  }
}
