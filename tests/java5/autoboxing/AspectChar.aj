public aspect AspectChar {

  // The pointcuts here expose context
  before(Character i): within(AutoboxingC) && call(* met*(..)) && args(i) {
    System.err.println("Character:"+i);
  }

  before(char i): within(AutoboxingC) && call(* met*(..)) && args(i) {
    System.err.println("char:"+i);
  }
}
