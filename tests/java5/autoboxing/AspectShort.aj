public aspect AspectShort {

  // The pointcuts here expose context
  before(Short i): within(AutoboxingS) && call(* met*(..)) && args(i) {
    System.err.println("Short:"+i);
  }

  before(short i): within(AutoboxingS) && call(* met*(..)) && args(i) {
    System.err.println("short:"+i);
  }
}
