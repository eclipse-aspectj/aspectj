public aspect AspectDouble {

  // The pointcuts here expose context
  before(Double i): within(AutoboxingD) && call(* met*(..)) && args(i) {
    System.err.println("Double:"+i);
  }

  before(double i): within(AutoboxingD) && call(* met*(..)) && args(i) {
    System.err.println("double:"+i);
  }
}
