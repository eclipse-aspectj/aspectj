public class StaticContexts {
    Object m() { return null; }

    static void s(Object o) {}
    class I extends C {
	I() {
	    super(StaticContexts.this);
	    s(StaticContexts.this);
	}
	I(int x) {
	    super(this);
	    s(this);
	}
	I(float x) {
	    super(m());
	    s(m());
	}
	static void foo() { //ERR: inner class can't have static member
	    s(StaticContexts.this);	    
	    s(this);
	    s(m());
	}
    }

    static class II extends C {
	II() {
	    super(StaticContexts.this);
	    s(StaticContexts.this);
	}
	II(int x) {
	    super(this);
	    s(this);
	}
	II(float x) {
	    super(m());
	    s(m());
	}
    }
}

class C {
    C(Object o) {}
}
