public class Code {
  public static void main(String[]argv) {
    System.out.println((new Code()) instanceof I);
  }
}

aspect Sub extends Super<Code> {}
