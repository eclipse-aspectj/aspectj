import java.util.Vector;
import org.aspectj.testing.*;

public class Counting1 {
    public static void main(String[] args) {

	Point pt1 = new Point(0, 0);
	Point pt2 = new Point(4, 4);
	Line  ln1  = new Line(pt1, pt2);
	
	System.out.println(MoveTracking.testAndClear());
	ln1.translate(3, 6);
	System.out.println(MoveTracking.testAndClear());

	System.out.println(pt1.getX());
	Mobility.disableMoves();
	ln1.translate(3, 6);	
	System.out.println(pt1.getX());
	
    }

    static class System {
        static O out = new O();
        static class O {
            public void println(Object o) {}
            public void println(int i) {}
            public void println(boolean b) {}                        
        }
    }
}

class FigureEditor {
    //...
}

class Figure {  
    Vector elements = new Vector();  
    //...
}



interface FigureElement {  
    public void translate(int dx, int dy);  
    //...
}

class Point implements FigureElement {  
    private int _x = 0, _y = 0;  

    Point(int x, int y) {
	_x = x;
	_y = y;
    }

    public void translate(int dx, int dy) {
	setX(getX() + dx);
	setY(getY() + dy);
    } 

    int getX() { return _x; }
    int getY() { return _y; }

    void setX(int x) { _x = x; }
    void setY(int y) { _y = y; }

    //...
}

class Line implements FigureElement {

	
    private Point _p1, _p2;

    Line (Point p1, Point p2) {
	_p1 = p1;
	_p2 = p2;
    }

    public void translate(int dx, int dy) {
	_p1.translate(dx, dy);   
	_p2.translate(dx, dy);
    }  
    
    Point getP1() { return _p1; }
    Point getP2() { return _p2; }
    
    void setP1(Point p1) { _p1 = p1; }
    void setP2(Point p2) { _p2 = p2; }

    //...
}

aspect JoinPointCounting {

  static int n = 0;
  static boolean enable = true;

  pointcut points(): 
      /*
    instanceof(*) && 
    !(receptions(* *.new(..)) || 
      executions(* *.new(..)));
      */

    call(* *.*(..)) ||
      //receptions(* *.*(..)) ||
      execution(* *.*(..));/* ||
    gets(* *.*) ||  
    sets(* *.*);
			  */
                          

  before(): points() && !within(JoinPointCounting) {
      if ( enable ) {
          String s = thisJoinPoint + "";
          Tester.check(s.indexOf("$") == -1, s + " contains a $");
      }
  }
}


aspect MoveTracking {

  static boolean flag = false;

  static boolean testAndClear() {
    boolean result = flag;
    flag = false;
    return result;
  }

  pointcut moves():
    call(void FigureElement.translate(int, int)) ||
    call(void Line.setP1(Point)) || 
    call(void Line.setP2(Point)) ||
    call(void Point.setX(int))   ||
    call(void Point.setY(int));

   after(): moves() {
     flag = true;
   }
 }

aspect Mobility { declare precedence: Mobility, MoveTracking;
	
    private static boolean enableMoves = true;
    
    static void enableMoves()  { enableMoves = true; }
    static void disableMoves() { enableMoves = false; }
    
    private int getSomething() { return 10; }
    
    void around(): MoveTracking.moves() {
    	int x = getSomething();
        if ( enableMoves || enableMoves )
            proceed(); //!!! in versions prior to 0.7b10 runNext is a
        //!!! method on the join point object, so the
        //!!! syntax of this call is slightly different
        //!!! than in the paper
    }
    
    void around(int i): args(i) && call(void *gaoijbal()) {
    	if (enableMoves) throw new RuntimeException("bad things");
    }
}

privileged aspect Foo {
	public static boolean getEnableMoves() {
		return Mobility.enableMoves;
	}
}
