import org.aspectj.testing.Tester;
import java.util.*;
import java.io.*;

public class SimpleGets {
    public static void main(String[] args) {
	Test t = new Test();
	t.go();

	System.out.println("s: " + t.s);

	Test.ss += "hi";

	//Tester.checkEqual(Test.calls, ", Test.go->Test, Test.foo->java.io.PrintStream");
    }
}


class Test {
    int x = 10;
    String s = "string";
    String s1 = "";

    static String ss = "";

    Test getT() {
	return new Test();
    }

    void go() {
	System.out.println(x);
	s = getT().s + ":went";
	Test.ss += "static";
	getT().s += ":more";
	Test t = this;
	t.s1 += "xxx";

	x += 10;
	x++;
	System.out.println(x + " == " + x--);
	System.out.println(x + " == " + x++);
	System.out.println(x-1 + " == " + --x);
    }
}

aspect NoteGets {
    
    static after(Test t) returning (Object v): gets(* t.*) { 
    	System.out.println("got it: " + v + " from " + t + " at " + thisJoinPoint); 
    }

    static after(String v): sets(String Test.*)[v][] { 
	new Test().s += "gi";
    	System.out.println("set: " + v + " at " + thisJoinPoint); 
    }
    
    static around(Object old, String v) returns String: sets(String Test.*)[old][v] { 
	new Test().s += "gi";
    	System.out.println("around set: " + old + " --> " + v + " at " + thisJoinPoint); 
	return proceed(old, v+"-advised");
    }
    
    static after(): sets(int Test.x) {
	int v = 0;
    	System.out.println("iset: " + v + " at " + thisJoinPoint); 
    }
 
    static after(Object v, Object old): sets(* Test.*)[old][v] { 
    	System.out.println("oset: " + old + " -> " + v + " at " + thisJoinPoint); 
    }

    static after(): gets(PrintStream java.lang.*.*) {
	System.out.println("got System.out");
    }
    
}
