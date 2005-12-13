

public aspect InterTypeDeclarations {
	
	private int I.x = 5;
	int I.y = 6;
	public int I.z = 7;
	
	private int I.getX() { return this.x; }
	int I.getY() { return this.y; }
	public int I.getZ() { return this.z; }
	
	private int C.x = 5;
	int C.y = 6;
	public int C.z = 7;
	
	private int C.getX() { return this.x; }
	int C.getY() { return this.y; }
	public int C.getZ() { return this.z; }
	
	private C.new(int x) { super(); this.x = x;}
	C.new(int x, int y) { this(x); this.y = y; }
	public C.new(int x, int y, int z) { this(x,y); this.z = z; } 
	
}


interface I {}

class C {}