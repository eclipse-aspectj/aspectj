public aspect AspectLong {

  // The pointcuts here expose context
  before(Long i): within(AutoboxingJ) && call(* met*(..)) && args(i) {
    System.err.println("Long:"+i);
  }

  before(long i): within(AutoboxingJ) && call(* met*(..)) && args(i) {
    System.err.println("long:"+i);
  }
}
