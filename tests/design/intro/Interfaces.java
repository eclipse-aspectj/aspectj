import org.aspectj.testing.Tester;

import java.util.*;

import org.aspectj.lang.reflect.*;

public class Interfaces {
    public static void main(String[] args) {
	String v = I.staticField;
	Tester.checkEqual(notes, "I.staticField", "static init of I");
	Tester.checkEqual(v, "I.staticField");
	clearNotes();

	I i = (I)new C();
	Tester.checkEqual(notes, 
	    "initialize-I instanceField-A* I.instanceField privateField-A* I.privateField-from-A privateField-A* I.privateField-from-B",
	    "inst init of I");
	Tester.checkEqual(i.instanceField, "I.instanceField");
	clearNotes();

	v = SubI.staticField;
	Tester.checkEqual(notes, "SubI.staticField", "static init of SubI");
	Tester.checkEqual(v, "SubI.staticField");
	clearNotes();

	SubI si = (SubI)new SubC();
	Tester.checkEqual(notes, 
	    "initialize-I instanceField-A* I.instanceField privateField-A* I.privateField-from-A privateField-A* I.privateField-from-B SubI.instanceField",
	    "inst init of SubI");
	Tester.checkEqual(si.instanceField, "SubI.instanceField");
	clearNotes();


        i.instanceField += "-XXX";
        Tester.checkEqual(notes, "I.instanceField-!I||A*", "I.instanceField set");
        Tester.checkEqual(i.instanceField, "I.instanceField-XXX");
    }

    private static List notes = new LinkedList();

    public static void clearNotes() {
	notes = new LinkedList();
	//System.out.println("***********************************************");
    }

    public static String note(String note) {
	notes.add(note);
	//System.out.println(note);
	return note;
    }
}

class C implements I {
    String instanceField = "C.instanceField";
}

class SubC extends C implements SubI {
    String instanceField = "SubC.instanceField";
}

interface I {
    // must follow standard Java rules
    String staticField = Interfaces.note("I.staticField");
}

interface SubI extends I {
    String staticField = Interfaces.note("SubI.staticField");
}
    


aspect A1 {
    public String SubI.instanceField = Interfaces.note("SubI.instanceField");

    public String I.instanceField = Interfaces.note("I.instanceField");
    private String I.privateField = Interfaces.note("I.privateField-from-A");
}

aspect A2 {
    private String I.privateField = Interfaces.note("I.privateField-from-B");
}    

aspect A3 {
    before(I i): !within(I||A*) && set(String I.*) && target(i) {
        Interfaces.note(thisJoinPoint.getSignature().toShortString()+"-!I||A*"); // +"::" + thisJoinPoint.getSourceLocation().getWithinType());
    }

    before(I i): within(I) && set(String I.*) && target(i) {
	Interfaces.note(thisJoinPoint.getSignature().getName()+"-I"); //toShortString());
    }
    before(I i): within(A*) && set(String I.*) && target(i) {
	Interfaces.note(thisJoinPoint.getSignature().getName()+"-A*"); //toShortString());
    }

    before(I i): initialization(I.new(..)) && target(i) {
        Interfaces.note("initialize-I");
    }
}
