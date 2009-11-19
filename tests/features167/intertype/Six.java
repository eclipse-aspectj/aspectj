aspect Six {
  intertype Target {
    int i = 5;
    String j = "hello";
  }
  public static void main(String[]argv) {
    Target target = new Target();
    System.out.println(target.j);
    System.out.println(target.i);
  }
}

class Target {}
