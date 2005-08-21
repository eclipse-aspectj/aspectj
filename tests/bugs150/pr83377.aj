
interface I extends Cloneable {
    public Object clone (); 
}

class C implements I {
//	public Object clone() {return this;}
	
}

privileged aspect A {
    declare parents : C implements java.lang.Cloneable;
    
	public Object C.clone () {
    	return null;
    }
}

