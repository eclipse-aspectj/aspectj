package langlib;
import org.aspectj.testing.Tester;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.Method;

import javax.swing.*;
import javax.swing.Action;

// DO NOT CHANGE LINEATION! WARNING NUMBERS DEPEND ON IT!  See grep below





/**
 * todo yet untested:
 * - dynamic calls
 */
public aspect PointcutsCW {
    declare error: Pointcuts.never() : "never";
    declare error: within(PointcutsCW) && Pointcuts.never() : "never";

    declare warning: Pointcuts.mainExecution() : "mainExecution";
    declare warning: Pointcuts.anyMethodExecution() : "anyMethodExecution";
    declare warning: Pointcuts.anyPublicMethodExecution() : "anyPublicMethodExecution";
    declare warning: Pointcuts.anyNonPrivateMethodExecution() : "anyNonPrivateMethodExecution";
    declare warning: Pointcuts.anyConstructorExecution() : "anyConstructorExecution";
    declare warning: Pointcuts.anyPublicConstructorExecution() : "anyPublicConstructorExecution";
    declare warning: Pointcuts.anyNonPrivateConstructorExecution() : "anyNonPrivateConstructorExecution";

    declare warning: Pointcuts.anyPublicFieldGet() : "anyPublicFieldGet";
    declare warning: Pointcuts.anyNonPrivateFieldGet() : "anyNonPrivateFieldGet";
    declare warning: Pointcuts.anyPublicFieldSet() : "anyPublicFieldSet";
    declare warning: Pointcuts.anyNonPrivateFieldSet() : "anyNonPrivateFieldSet";
    declare warning: Pointcuts.withinSetter() : "withinSetter";
    declare warning: Pointcuts.withinGetter() : "withinGetter";
    declare warning: Pointcuts.anyNonPublicFieldSetOutsideConstructorOrSetter() : "anyNonPublicFieldSetOutsideConstructorOrSetter";

    declare warning: Pointcuts.anyRunnableImplementation() : "anyRunnableImplementation";
    declare warning: Pointcuts.anyGetSystemErrOut() : "anyGetSystemErrOut";
    declare warning: Pointcuts.anySetSystemErrOut() : "anySetSystemErrOut";
    declare warning: Pointcuts.withinAnyJavaCode() : "withinAnyJavaCode"; // XXX
    declare warning: Pointcuts.notWithinJavaCode() : "notWithinJavaCode"; // XXX
    declare warning: Pointcuts.toStringExecution() : "toStringExecution";    
    declare warning: Pointcuts.anyThreadConstruction() : "anyThreadConstruction";
    declare warning: Pointcuts.anyJavaIOCalls() : "anyJavaIOCalls";
    declare warning: Pointcuts.anyJavaAWTOrSwingCalls() : "anyJavaAWTOrSwingCalls";
    declare warning: Pointcuts.cloneImplementationsInNonCloneable() : "cloneImplementationsInNonCloneable";
    declare warning: Pointcuts.runImplementationsInNonRunnable() : "runImplementationsInNonRunnable";
    declare warning: Pointcuts.anySystemReflectiveCalls() : "anySystemReflectiveCalls";
    declare warning: Pointcuts.anySystemClassLoadingCalls() : "anySystemClassLoadingCalls";
    declare warning: Pointcuts.anySystemProcessSpawningCalls() : "anySystemProcessSpawningCalls";
    declare warning: Pointcuts.mostThrowableReadCalls() : "mostThrowableReadCalls";
    declare warning: Pointcuts.exceptionWrappingCalls() : "exceptionWrappingCalls";
        
    public static int publicStaticInt;
    public int publicInt;
    private static int privateStaticInt;
    private int privateInt;
    static int defaultStaticInt;
    int defaultInt;
    
    //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
    public static void main(String[] list) {
        new PointcutsCW().toString(); // RT cflowMainExecution
    }
    
    private PointcutsCW() {} // CW anyConstructorExecution
    
    public PointcutsCW(int i) {} // CW anyConstructorExecution, anyPublicConstructorExecution, anyNonPrivateConstructorExecution

    PointcutsCW(String s) { // CW anyConstructorExecution, anyNonPrivateConstructorExecution

        defaultInt = 0;  // CW anyNonPrivateFieldSet

    }

    //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
    public String toString() {// CW toStringExecution 
        return "";
    }   

    private int perrorCode() { } // CW anyMethodExecution
    
    private void setInt() { // CW anyMethodExecution, 
    
        defaultInt = 0;  // CW anyNonPrivateFieldSet, withinSetter
    
    }
    
    private int getInt() {
    
        return defaultInt;   // CW anyNonPrivateFieldGet, withinGetter
    
    }
    
    static class NotRunnable {
        //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
        public void run() { // CW runImplementationsInNonRunnable
        }
    }

    static class R implements Runnable { // CW anyRunnableImplementation
        
        //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
        public void run() {
        
        }

        //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
        public Object clone() { // CW cloneImplementationsInNonCloneable
            return null; 
        }
        
    }

    //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
    public static void pserrorCode() throws IOException { 
        
        i = publicStaticInt; // CW anyPublicFieldGet, anyNonPrivateFieldGet
        
        i = publicInt; // CW anyPublicFieldGet, anyNonPrivateFieldGet
        
        i = privateStaticInt;
        
        i = privateInt;
        
        i = defaultStaticInt; // CW anyNonPrivateFieldGet
        
        i = defaultInt; // CW anyNonPrivateFieldGet
        
        publicStaticInt = 1;  // CW anyPublicFieldSet, anyNonPrivateFieldSet
        
        publicInt = 1; // CW anyPublicFieldSet, anyNonPrivateFieldSet
        
        // for these 4: CW anyNonPublicFieldSetOutsideConstructorOrSetter
        privateStaticInt = 1;
        
        privateInt = 1;
        
        defaultStaticInt = 1;   // CW anyNonPrivateFieldSet
        
        defaultInt = 1; // CW anyNonPrivateFieldSet
        
        System.out.println(""); // CW anyGetSystemErrOut, anyNonPrivateFieldGet, anyPublicFieldGet

        System.err.println(""); // CW anyGetSystemErrOut, anyNonPrivateFieldGet anyPublicFieldGet

        new Thread((Runnable)null); // CW anyThreadConstruction

        FileReader fr = new FileReader("none"); // CW anyJavaIOCalls
        
        int i = fr.read();  // CW anyJavaIOCalls
        
        DefaultListModel model = new DefaultListModel(); // CW anyJavaAWTOrSwingCalls
        
        model.addElement(null); // CW anyJavaAWTOrSwingCalls
        
        Button button = new Button();  // CW anyJavaAWTOrSwingCalls
        
        button.addActionListener(null); // CW anyJavaAWTOrSwingCalls

        String myName = PointcutsCW.class.getName();
        
        Class me = Class.forName(myName);  // CW anySystemClassLoadingCalls
        
        Method m = me.getDeclaredMethod("notFound", new Class[]{}); // CW anySystemReflectiveCalls
        
        Process p = Runtime.exec("ls"); // CW anySystemProcessSpawningCalls
        
        Error e = new Error("hello");

        e.getMessage(); // CW mostThrowableReadCalls
        
        e.printStackTrace(); // CW mostThrowableReadCalls

        e.getClass(); // CW mostThrowableReadCalls

    }
    
}

aspect DynamicTests {
    static {
        Tester.expectEvent("mainExecution");
        Tester.expectEvent("cflowMainExecution");
        Tester.expectEvent("adviceCflow");
        Tester.expectEvent("notInAdviceCflow");
    }
    after(PointcutsCE pointcutsCE) returning : target(pointcutsCE) 
            && Pointcuts.cflowMainExecution() && call(String toString()) {
        String targ = pointcutsCE.toString();
        Tester.event("cflowMainExecution");
        Tester.event("adviceCflow");
    }

    after(PointcutsCE pointcutsCE) returning : target(pointcutsCE) 
            && notInAdviceCflow() && call(String toString()) {
        Tester.event("notInAdviceCflow"); // should only get one of these
    }

    after() returning : within(PointcutsCE) && Pointcuts.mainExecution() {
        Tester.event("mainExecution"); // also cflowMainExecution
        Tester.checkAllEvents();
    }
    
}
/*
  grep -n " CW" PointcutsCW.java  \
    | sed 's|^\(.*\)\:.*\/\/*CW \(.*\)$|<message kind="warning" line="\1" text="\2"/>|' \
    > messages.txt

*/