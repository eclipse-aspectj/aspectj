import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }
  
  public static void test() { 
   
    Pos p1 = new Pos();
    Pos p2 = new Pos(); 
    Pos p3 = new Pos();

    Foo f1 = Foo.aspectOf(p1);
    Foo f2 = Foo.aspectOf(p2);
    Foo f3 = Foo.aspectOf(p3);

    p1.move(1, 2);
    Tester.checkEqual(p1.getX(), 1, "p1.x");
    Tester.checkEqual(p1.getY(), 2, "p1.y");

    p2.move(1, 2);
    Tester.checkEqual(p2.getX(), 1, "p2.x");
    Tester.checkEqual(p2.getY(), 2, "p2.y");
   
    p3.move(1, 2);
    Tester.checkEqual(p3.getX(), 1, "p3.x");
    Tester.checkEqual(p3.getY(), 2, "p3.y");

    Tester.checkEqual(f1.count, 3, "f1.count");
    Tester.checkEqual(f2.count, 3, "f2.count");
    Tester.checkEqual(f3.count, 3, "f3.count");
    Tester.checkEqual(Bar.countx, 9, "Bar.countx");
  }
}

class Pos {

  int x = 0;
  int y = 0;

  int getX() { 
    return(x);
  }

  int getY() {
    return(y);
  }
  
  void move(int newX, int newY) {
    x=newX;
    y=newY;
  }
}

aspect Foo pertarget(target(Pos)) {
  int count = 0;

  before (): ( call(* getX(..)) || 
               call(* getY(..)) ||
               call(* move(..))  ) {
      count++;
  }
}     

aspect Bar {
  static int countx = 0;
  
  /*static*/ before (): target(Pos) && 
                    ( call(* getX(..)) || 
                      call(* getY(..)) ||
                      call(* move(..))  ) {
      countx++;
  }
}
