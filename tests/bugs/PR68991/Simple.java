import java.util.*;

aspect Aspect {
	
	public static List tjps = new ArrayList();
	public static List values = new ArrayList();
	public static List ejps = new ArrayList();
	
	public  int C.m = 13;
    private int C.n = 13;

    before() : get(* C.*) {
    	tjps.add(thisJoinPointStaticPart.toString());
    	ejps.add(thisEnclosingJoinPointStaticPart.toString());
    	//System.out.println("get field "+thisJoinPointStaticPart);
    }

    before(int x) : set(* C.*) && args(x)  {
    	tjps.add(thisJoinPointStaticPart.toString());
    	ejps.add(thisEnclosingJoinPointStaticPart.toString());
    	values.add(new String(thisJoinPointStaticPart+"="+new Integer(x)));
    	//System.err.println("set field "+thisJoinPointStaticPart);
    }
    
    public void C.foo() {
    	m++;
    	n++;
    }

}

class C {
  //  int m = 20;
}

public class Simple {

    public static void main(String[] args) {
    	C c = new C();
    	c.foo();
    	System.err.println("\nSummaryJPs:"+Aspect.tjps);
    	System.err.println("\nSummaryEJPs:"+Aspect.ejps);
    	System.err.println("\nSummaryVals:"+Aspect.values);
    	// Ought to have a nicer signature for the ejpsp in the case of an initializer ...
    	chkNext(Aspect.tjps,"set(int C.m)");chkNext(Aspect.values,"set(int C.m)=13");chkNext(Aspect.ejps,"execution(void Aspect.ajc$interFieldInit$Aspect$C$m(C))");
    	chkNext(Aspect.tjps,"set(int C.n)");chkNext(Aspect.values,"set(int C.n)=13");chkNext(Aspect.ejps,"execution(void Aspect.ajc$interFieldInit$Aspect$C$n(C))");
    	chkNext(Aspect.tjps,"get(int C.m)");                                         chkNext(Aspect.ejps,"execution(void C.foo())");
    	chkNext(Aspect.tjps,"set(int C.m)");chkNext(Aspect.values,"set(int C.m)=14");chkNext(Aspect.ejps,"execution(void C.foo())");
    	chkNext(Aspect.tjps,"get(int C.n)");                                         chkNext(Aspect.ejps,"execution(void C.foo())");
    	chkNext(Aspect.tjps,"set(int C.n)");chkNext(Aspect.values,"set(int C.n)=14");chkNext(Aspect.ejps,"execution(void C.foo())");
    }
    
    public static void chkNext(List l,String expected) {
    	String s = (String)l.remove(0);
    	if (!s.equals(expected)) throw new RuntimeException("Expected next thing on list to be '"+expected+"' but it was '"+s+"'");
    }

}