public aspect AspectFloat {

  // The pointcuts here expose context
  before(Float i): within(AutoboxingF) && call(* met*(..)) && args(i) {
    System.err.println("Float:"+i);
  }

  before(float i): within(AutoboxingF) && call(* met*(..)) && args(i) {
    System.err.println("float:"+i);
  }
}
