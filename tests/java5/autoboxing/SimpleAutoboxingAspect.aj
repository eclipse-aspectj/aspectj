public aspect SimpleAutoboxingAspect {

  // The pointcuts here expose context
  before(Integer i): within(SimpleAutoboxing) && call(* met*(..)) && args(i) {
    System.err.println("Matching by Integer:"+i);
  }

  before(int i): within(SimpleAutoboxing) && call(* met*(..)) && args(i) {
    System.err.println("Matching by int:"+i);
  }
}
