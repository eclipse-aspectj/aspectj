package main;

import org.aspectj.testing.Tester;

public class Target {
    void run() { 
        Tester.event("Target.run()");
    }
}
