public aspect AspectByte {

  // The pointcuts here expose context
  before(Byte i): within(AutoboxingB) && call(* met*(..)) && args(i) {
    System.err.println("Byte:"+i);
  }

  before(byte i): within(AutoboxingB) && call(* met*(..)) && args(i) {
    System.err.println("byte:"+i);
  }
}
