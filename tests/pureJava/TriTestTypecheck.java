class A {}
interface G {}
class E extends A implements G {}

public class TriTestTypecheck {

    public static void main(String[] args) {}

    void byteCall(byte b) {}
    void shortCall(short s) {}
    void charCall(char c) {}
    void intCall(int i) {}
    void longCall(long l) {}
    void floatCall(float f) {}
    void doubleCall(double d) {}

    void booleanCall(boolean t) {}

    void aCall(A a) {}
    void eCall(E e) {}
    void gCall(G g) {}

    void foo(boolean t, byte b, short s, char c, int i, long l, float f, double d, A a, E e, G g) {

	byteCall(t ? 37 : b);
	byteCall(t ? b : 37);	
	byteCall(t ? b : b);	

	shortCall(t ? 37 : s);
	shortCall(t ? s : 37);	
	shortCall(t ? b : s);
	shortCall(t ? s : b);	
	shortCall(t ? s : s);	

	charCall(t ? 37 : c);
	charCall(t ? c : 37);	
	charCall(t ? c : c);	

	intCall(t ? 257 : b);
	intCall(t ? b : 257);
	intCall(t ? 65537 : s);
	intCall(t ? s : 65537);
	intCall(t ? -1 : c);
	intCall(t ? c : -1);
	intCall(t ? i : i);	

	longCall(t ? l : b);
	longCall(t ? b : l);
	longCall(t ? l : s);
	longCall(t ? s : l);
	longCall(t ? l : c);
	longCall(t ? c : l);
	longCall(t ? l : i);	
	longCall(t ? i : l);	
	longCall(t ? l : l);	
	
	floatCall(t ? f : b);
	floatCall(t ? b : f);
	floatCall(t ? f : s);
	floatCall(t ? s : f);
	floatCall(t ? f : c);
	floatCall(t ? c : f);
	floatCall(t ? f : i);	
	floatCall(t ? i : f);	
	floatCall(t ? f : l);	
	floatCall(t ? l : f);	
	floatCall(t ? f : f);	

	doubleCall(t ? d : b);
	doubleCall(t ? b : d);
	doubleCall(t ? d : s);
	doubleCall(t ? s : d);
	doubleCall(t ? d : c);
	doubleCall(t ? c : d);
	doubleCall(t ? d : i);	
	doubleCall(t ? i : d);	
	doubleCall(t ? d : l);	
	doubleCall(t ? l : d);	
	doubleCall(t ? d : f);	
	doubleCall(t ? f : d);	
	doubleCall(t ? d : d);	

	booleanCall(t ? t : t);

	aCall(t ? a : null);
	aCall(t ? null : a);
	aCall(t ? a : e);
	aCall(t ? e : a);
	aCall(t ? a : a);

	gCall(t ? g : null);
	gCall(t ? null : g);
	gCall(t ? g : e);
	gCall(t ? e : g);
	gCall(t ? g : g);

	eCall(t ? e : e);
    }
}

