
//package debugger;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.io.*;
import java.util.*;
import org.aspectj.tools.debugger.*;

/**
 * ThreadTester.java
 *
 *
 * Created: Wed Sep 27 13:56:44 2000
 *
 * @author <a href="mailto:palm@parc.xerox.com"Jeffrey Palm</a>
 */

public class ThreadTester extends Tester {

    public static void main(String[] args) {
        new Main(new ThreadTester(false), args);
    }

    public ThreadTester(boolean d) {
        super(d);
    }

    public String getClassName() {
        return "AJDBThreads";
    }

    public boolean test() {
        db();
        try {
            stopon(43);
            stop1();
            startTest();
            quit();
            return true;
        } catch (DebuggerException de) {
            de.printStackTrace();
        }
        return false;
    }

    protected void stop1() {
        d.addStopListener(new StopAdapter() {

                int times = 0;
                int stopTimes = 0;
                int max = 5;
                int stopMax = 3;
                String thread = "";
                Pair[] pairs = new Pair[max];

                class Pair {
                    String thread;
                    int time;
                    Pair(String thread, int time) {
                        this.thread = thread;
                        this.time = time;
                    }
                }
                
                public void breakpointEvent(BreakpointEvent e) {
                    try {
                        String threadName = d.getDefaultThread().name();
                        msg("stop times=" + times + " thread=" + threadName);
                        if ((++times) < max) {                            
                            thread = threadName;
                            Pair pair = new Pair(thread, times);
                            pairs[times] = pair;
                            step();
                        } else {
                            quit();
                        }
                    } catch (DebuggerException de) {
                        de(de);
                    }
                }

                public void stepEvent(StepEvent se) { 
                    try {
                        ThreadReference threadRef = d.getDefaultThread();
                        check(pairs[times].thread.equals(thread), "Should step in *one* thread");
                        msg("\ttimes=" + times + ":" + stopTimes  + " thread=" + threadRef.name());
                        if ((++stopTimes) < stopMax) {
                            step();
                        } else {
                            stopTimes = 0;
                            cont();
                        }
                    } catch (DebuggerException de) {
                        de(de);
                    }
                }                
            });
    }

    protected long getMaxStallTime() {
        return (long) 3 * super.getMaxStallTime();
    }
}
