public aspect AspectInteger {

  // The pointcuts here expose context
  before(Integer i): within(AutoboxingI) && call(* met*(..)) && args(i) {
    System.err.println("Matching by Integer:"+i);
  }

  before(int i): within(AutoboxingI) && call(* met*(..)) && args(i) {
    System.err.println("Matching by int:"+i);
  }
}
