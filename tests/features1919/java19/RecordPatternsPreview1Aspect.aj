public aspect RecordPatternsPreview1Aspect {
  public static void main(String[] args) {
    doSomething(new Point(2, 7));
    doSomething(new Rectangle(
      new ColoredPoint(new Point(1, 6), Color.RED),
      new ColoredPoint(new Point(4, 6), Color.BLUE)
    ));
  }

  public static void doSomething(Object object) {
    System.out.println("Doing something with " + object);
  }

  before(Object object): execution(* doSomething(*)) && args(object) {
    if (object instanceof Point p) {
      int x = p.x();
      int y = p.y();
      System.out.println(x + y);
    }
    if (object instanceof Point(int x, int y))
      System.out.println(x * y);

    if (object instanceof Rectangle(ColoredPoint ul, ColoredPoint lr))
      System.out.println("Upper-left color: " + ul.c());
    if (object instanceof Rectangle(ColoredPoint(Point p, Color c), ColoredPoint lr))
      System.out.println("Upper-left color: " + c);
    if (object instanceof Rectangle(ColoredPoint(Point(var x, var y), var c), var lr))
      System.out.println("Upper-left x coordinate: " + x);
  }
}

record Point(int x,int y){}
enum Color { RED, GREEN, BLUE }
record ColoredPoint(Point p, Color c) {}
record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}
