import org.aspectj.testing.Tester;

public class NotAndDeclaringTypes {

  public static void main (String args []) {
    Rectangle r = new Rectangle ();
    Square s = new Square ();

    r.getSurface();
    Tester.checkAndClearEvents(new String[] { "advice" });

    s.getSurface();

    Tester.checkAndClearEvents(new String[] { });
  } 
}

class Rectangle {
  public String toString () { return "Rectangle"; }
  public int getSurface () { return 100; }
}

class Square extends Rectangle {
  public String toString () { return "Square"; }
  public int getSurface () { return 200; }
}

aspect Concern {

  pointcut pc () : call (int Rectangle.getSurface ())
                   && !call (int Square.getSurface ());

  before () : pc () {
      Tester.event("advice");
  }
}

