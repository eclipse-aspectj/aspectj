aspect Five {
  intertype Target {
    int i = 5;
  }
  public static void main(String[]argv) {
    Target target = new Target();
    System.out.println(target.i);
  }
}

class Target {}
