import org.aspectj.lang.annotation.*;

public aspect AtAspectJDeclareParents {
	
	@DeclareParents(value="C",defaultImpl=Impl.class)
	private I implementedInterface;
	
}

class C {}

interface I{
	
	int getX();
	
	void setX(int x);
	
}

class Impl implements I {
	
	private int x;
	
	public int getX() { return this.x; }
	
	public void setX(int x) { this.x = x; }
}