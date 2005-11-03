// -*- Mode: java; -*-
import org.aspectj.testing.Tester;

public class Driver {

  public static void main(String[] args) { test(); }

  public static void test() {
    MagicKey key = new MagicKey();
    
    Pos p1 = new Pos(key);

    Tester.checkEqual(p1.getOther(), 1, "introduced value");
  }
}
class Pos {

  int _x = 0;
  int _y = 0;

  public int getX() { 
    return(_x);
  }

  public int getY() {
    return(_y);
  }
  
  public void move(int x, int y) {
    _x = x;
    _y = y;
  }
}

aspect Foo {
  // this has been declared illegal syntax as of 4/19/99, see below for fix
  // introduce public int Pos.id = 1, Pos.other;

    //introduction Pos {
    //XXX might want to test for this in the future
    //public int Pos.id=1, Pos.other;
    public int Pos.id=1;
    public int Pos.other;
    
    int Pos.getOther() {
        return other;
    }
        
    Pos.new(MagicKey key) { 
    		   this();
            other = id;
            id = getOther();
        }
        //}
}
  
class MagicKey {}
