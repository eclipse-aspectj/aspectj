package org.xyz; import anns.*;
import java.util.List;
import java.util.ArrayList;

class C {
	
	@SensitiveData List l = new ArrayList();
	
	List l2 = new ArrayList();
	
	D d = new D();
	
	@SensitiveData D d2 = new D();
	
}

@SensitiveData @Persisted class D {
	@Transaction void update() {}
	void read() {}
}

@Immutable interface I {}

class E {
	
	@Cachable Object expensive = null;
	
	public I getI() {
		return null;
	}
	
}

