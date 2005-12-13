import org.aspectj.lang.annotation.*;

public aspect AtAspectJDeclareParents {
	
	@DeclareParents("C")
	public static I mixin = new Impl();
	
}

class C {}

interface I{}

class Impl implements I {
	
	private int x;
	
	public int getX() { return this.x; }
	
	public void setX(int x) { this.x = x; }
}