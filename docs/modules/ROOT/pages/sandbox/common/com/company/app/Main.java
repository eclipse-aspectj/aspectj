
package com.company.app;

import java.util.Arrays;
import org.aspectj.lang.SoftException;

public class Main implements Runnable {
    public static void main(String[] argList) {
        new Main().runMain(argList);
    }
    
    String[] input;
    
    void spawn() {        
        new Thread(this, toString()).start(); // KEEP CE 15 declares-factory
    }

    public void runMain(String[] argList) {
        this.input = argList;
        run();
    }
    
    public void run() {
        String[] input = this.input;
        String s = ((null == input) || (0 == input.length))
            ? "[]"
            : Arrays.asList(input).toString();
        System.out.println("input: " + s);
        try {
            doDangerousThings();           // KEEP CW 30 declares-exceptionSpelunking
        } catch (AppException e) {         // KEEP CW 31 declares-exceptionSpelunking
            e.printStackTrace(System.err);
        }
    }
    
    private void doDangerousThings() throws AppException {   // KEEP CW 38
          
    }

}