class Test<O> {
  O field;
}

class P {
  public static void main(String[] argv) {
     new Test<Integer>().field = 42;
  }
}
