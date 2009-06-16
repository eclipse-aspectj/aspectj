package g;

import f.AClass;

public aspect AnAspect {
	public int AClass.xxxx;
	
	public int AClass.y() {
		return 0;
	}
	
	AClass.new() {
		this();
	}
}  
