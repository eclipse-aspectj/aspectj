package org.aspectj.langlib;
import org.aspectj.testing.Tester;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import javax.swing.*;
import javax.swing.Action;




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
    //declare warning: Pointcuts.withinAnyJavaCode() : "withinAnyJavaCode"; // XXX
    //declare warning: Pointcuts.notWithinJavaCode() : "notWithinJavaCode"; // XXX
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
    declare warning: Pointcuts.anyCollectionWriteCalls() : "anyCollectionWriteCalls";
    //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
    public static void main(String[] list) {
        new MemberTests(0).toString(); // RT cflowMainExecution
    }
}
class MemberTests {        
    public static int publicStaticInt;
    public int publicInt;
    private static int privateStaticInt;
    private int privateInt;
    static int defaultStaticInt;
    int defaultInt;
        
    private MemberTests() {} // CW anyConstructorExecution
    
    public MemberTests(int i) {} // CW anyConstructorExecution, anyPublicConstructorExecution, anyNonPrivateConstructorExecution

    MemberTests(String s) { // CW anyConstructorExecution, anyNonPrivateConstructorExecution

        defaultInt = 0;  // CW anyNonPrivateFieldSet

    }

    //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
    public String toString() {// CW toStringExecution 
        return "";
    }   

    private int pperrorCode() { return 0; } // CW anyMethodExecution
    
    private void setInt(int i) { // CW anyMethodExecution, 
    
        defaultInt = i;  // CW anyNonPrivateFieldSet, withinSetter
    
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
            int i = 1;   // hmm -- getting 110 here instead of 109?
        }

        //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
        public Object clone() { // CW cloneImplementationsInNonCloneable
            return null; 
        }
        
    }

    //  CW anyMethodExecution, anyPublicMethodExecution, anyNonPrivateMethodExecution
    public void perrorCode() throws Exception { 
        
        int i = publicStaticInt; // CW anyPublicFieldGet, anyNonPrivateFieldGet
        
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
        
        System.out.println(""); // CW anyGetSystemErrOut, anyNonPrivateFieldGet, anyPublicFieldGet, anyJavaIOCalls

        System.err.println(""); // CW anyGetSystemErrOut, anyNonPrivateFieldGet anyPublicFieldGet, anyJavaIOCalls
        
        new Thread((Runnable)null); // CW anyThreadConstruction, anyConstructorExecution, anyNonPrivateConstructorExecution

        FileReader fr = new FileReader("none"); // CW anyJavaIOCalls
                
        i = fr.read();  // CW anyJavaIOCalls

        DefaultListModel model = new DefaultListModel(); // CW anyJavaAWTOrSwingCalls
        
        model.addElement(null); // CW anyJavaAWTOrSwingCalls

        Button button = new Button();  // CW anyJavaAWTOrSwingCalls
        
        button.addActionListener(null); // CW anyJavaAWTOrSwingCalls

        String myName = PointcutsCW.class.getName();    // CW anySystemClassLoadingCalls, mostThrowableReadCalls b/c of in-bytecode conversion from ClassNotFoundException to NoClassDefFoundError
        
        Class me = Class.forName(myName);  // CW anySystemClassLoadingCalls
        
        Method m = me.getDeclaredMethod("notFound", new Class[]{}); // CW anySystemReflectiveCalls
        
        Process p = Runtime.getRuntime().exec("ls"); // CW anySystemProcessSpawningCalls
        
        Error e = new Error("hello");

        e.getMessage(); // CW mostThrowableReadCalls
        
        e.printStackTrace(); // CW mostThrowableReadCalls

        e.getClass(); // not mostThrowableReadCalls b/c getClass() is Object
        
        List list = new ArrayList();
        list.add("one"); // CW anyCollectionWriteCalls
        
        // actually not writing, but staticly might
        list.remove("two"); // CW anyCollectionWriteCalls
        
        list.removeAll(Collections.EMPTY_LIST); // CW anyCollectionWriteCalls, anyPublicFieldGet, anyNonPrivateFieldGet
        
        list.retainAll(list); // CW anyCollectionWriteCalls

    }
    
}


aspect DynamicTests {
    DynamicTests() {
        int i = 1;    // CW anyConstructorExecution, anyNonPrivateConstructorExecution XXX shows as 190, not 189?
    }
    static {
        Tester.expectEvent("mainExecution");
        Tester.expectEvent("cflowMainExecution");
        Tester.expectEvent("adviceCflow");
        Tester.expectEvent("notInAdviceCflow");
    }
    after(MemberTests memberTests) returning : target(memberTests) 
            && Pointcuts.cflowMainExecution() && call(String toString()) 
            && !within(DynamicTests) {
        String targ = memberTests.toString();
        Tester.event("cflowMainExecution");
        Tester.event("adviceCflow");
    }

    after() returning : target(MemberTests) 
            && Pointcuts.notInAdviceCflow() && call(String toString()) {
        Tester.event("notInAdviceCflow"); // should only get one of these
    }

    after() returning : within(PointcutsCW) && Pointcuts.mainExecution() {
        Tester.event("mainExecution"); // also cflowMainExecution
        Tester.checkAllEvents();
    }
    
}
/*
  grep -n " CW" PointcutsCW.java  \
    | sed 's|^\(.*\)\:.*\/\/*CW \(.*\)$|<message kind="warning" line="\1" text="\2"/>|' \
    > messages.txt

*/