//package debugger;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.io.*;
import java.util.*;
import org.aspectj.tools.debugger.*;

public class ArgumentTester extends Tester {

    public static void main(String[] args) {
        new Main(new ArgumentTester(false), args);
    }
    
    public ArgumentTester(boolean b) {
        super(b);
    }

    public String getClassName() {
        return "Arguments";
    }

    public boolean test() {
        db();
        try {
            setRunArgs("0 1 2");
            stopon(19);
            stop(3);
            startTest();
            setRunArgs("0 1 2 3 4 5 6");
            stopon(19);
            stop(7);
            startTest();            
            quit();
            return true;
        } catch (DebuggerException de) {
            de.printStackTrace();
        }
        return false;
    }

    protected void stop(final int max) {
        d.addStopListener(new StopAdapter() {
                int times = -1;
                public void breakpointEvent(BreakpointEvent e) {
                    try {
                        String value = print("s") + "";
                        String str = "\"" + times + "\"";
                        if ((times++) != -1) {
                            check(value.equals(str), value + "!=" + str);
                        }
                        if (times < max) {
                            cont();
                        } else {
                            clear(19);
                            d.removeStopListener(this);
                        }
                    } catch (DebuggerException de) {
                        de(de);
                    }
                }
            });
    }
}
