
package mj.compiler.ast;

import java.util.Vector;

class Sequence<T extends AST> extends AST {
	
	private Vector<T> elements = new Vector<T>();
	
	public Sequence(T element) {
//		super(element);
		elements.add(element);
    }
    
	public int length()       { return elements.size(); }
	public T elementAt(int i) { return elements.elementAt(i); }
}

class AST<T> {
    public AST() { } 
    
    public AST(T element) {  }
}