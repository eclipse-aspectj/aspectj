import java.util.*;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import org.aspectj.testing.*;

public class Binkley2 {
    public static void main(String[] args) {
        // catch the static init early
        C c = new C();
        
        Art.enable = true;
        Ar.enable  = false;
        new C().foo();
        
        Art.enable = false;
        Ar.enable  = true;
        new C().foo();
        Post.checkAll();    
    }
}

class C {
    public int x = 0;
    public void foo() 
    {
        x = 1;
        x = 2;
    }
    
}

class Post {
    static List haves = new Vector();
    static String[] wants = new String[] {
        "preinitialization(C())-Ar-0", 
        "initialization(C())-Ar-0", 
        "execution(C())-Ar-0", 
        "set(C.x)-Ar-0",
        "execution(C.foo())-Ar-0", 
        "set(C.x)-Ar-0",
        "set(C.x)-Ar-0",
        
        "preinitialization(C())-Art-0", 
        "initialization(C())-Art-0", 
        "execution(C())-Art-1",
        "set(C.x)-Art-2",
        "execution(C.foo())-Art-0",
        "set(C.x)-Art-1",
        "set(C.x)-Art-2",
        };

    static void post(JoinPoint jp, String name, int num) {
    	//System.out.println("have: " + jp.toShortString() + "-" + name + "-" + num);
	haves.add(jp.toShortString() + "-" + name + "-" + num);
    }
    static void checkAll() {
	Tester.checkEqual(haves, wants, "haves != wants");
    }
}

aspect Ar percflow(pc()){
    pointcut pc() : within(C) ;
    int count = 0;
    static boolean enable = false;
    before(): pc() {
        if ( enable ) {
            Post.post(thisJoinPoint, "Ar", count++);
        }
    }
}

/*
 * I'm trying to simulate eachcflowrootop.
 */
aspect Art percflow(Art.pc() && !cflowbelow(Art.pc()))
{
    pointcut pc() : within(C) ;
    //pointcut pctop(): pc() && !cflow(pc());                  (see above)
    int count = 0;
    static boolean enable = false;
    
    before(): pc() {
        if ( enable ) {
            Post.post(thisJoinPoint, "Art", count++);
        }
    }
}

