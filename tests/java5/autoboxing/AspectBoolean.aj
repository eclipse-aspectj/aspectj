public aspect AspectBoolean {

  // The pointcuts here expose context
  before(Boolean i): within(AutoboxingZ) && call(* met*(..)) && args(i) {
    System.err.println("Boolean:"+i);
  }

  before(boolean i): within(AutoboxingZ) && call(* met*(..)) && args(i) {
    System.err.println("boolean:"+i);
  }
}
