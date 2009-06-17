package g;

import f.AClass;
import f.*;

public aspect AnAspect {
	public int AClass.xxxx;
	
	public int AClass.y() {
		return 0;
	}
	
	AClass.new() {
		this();
	}
}  
