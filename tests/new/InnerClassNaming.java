import java.awt.*;
import java.awt.event.*;
import org.aspectj.testing.Tester;

/*
 * I would like to listen to events fired by a whiteboard swing widget.
 * When I listen to method invocations from the widget itself in 
 * an aspect,
 * there
 * is no problem (like say getPreferredSize()).
 * 
 * But, when I want to listen to events fired by a mouse adapter 
 * from which
 * I derived
 * an inner class in the whiteboard like this :
 *
 * SOLUTION: Replace all super.inner to super$inner.
 */

public class InnerClassNaming {
  public static void main(String[] args) {
    new InnerClassNaming();
  }

  InnerClassNaming() {
    MouseListener listener = new MyListener();
    addMouseListener(listener);
  }

  void addMouseListener(MouseListener listener) {}
  
  class MyListener extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      System.out.println("mousPressed: " + e);
    }
  }  
}

aspect MyAspect /*of eachobject(instanceof(InnerClassNaming.MyListener))*/ {
  
  pointcut pressed(InnerClassNaming.MyListener ls):
      //instanceof(ls) && executions(* mousePressed(..));
      this(InnerClassNaming.MyListener) &&
      target(ls) &&
      execution(* mousePressed(..));
  
  before(InnerClassNaming.MyListener ls): pressed(ls) {
    System.out.println(thisJoinPoint);
  }
}
