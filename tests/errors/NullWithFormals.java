import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class NullWithFormals {

    public void realMain(String[] args) {
        new InnerWindowAdapter().windowClosing(null);
    }

    static class InnerWindowAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent we) {
        }
    } 

    public static void main(String[] args) {
        new NullWithFormals().realMain(args);
    }
}


aspect AspectW {

    pointcut pc0(WindowEvent we, String str) : instanceof(WindowAdapter) && executions(void windowClosing(we));
    static before(WindowEvent we, String str): pc0(we, str) {
        System.out.println(thisJoinPoint);
    }   

    pointcut pc1(String str, WindowEvent we) : instanceof(WindowAdapter) && executions(void windowClosing(we));
    static before(String str, WindowEvent we): pc1(str, we) {
        System.out.println(thisJoinPoint);
    }

    pointcut pc2(WindowEvent we) : instanceof(WindowAdapter) && executions(void windowClosing(we));
    static before(WindowEvent we): pc2(we) {
        System.out.println(thisJoinPoint);
    }

}
