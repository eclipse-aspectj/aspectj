/* initialisers of intertype fields should match field set pointcuts.

In the example below, the output should be

set field set(int C.n)
set field set(int C.m)
get field get(int C.n)
set field set(int C.n)

but the first field set (of C.n) is not picked up.
*/



aspect Aspect {
	

 private int C.n = 13;

 before() : get(* C.*) {
	System.err.print(":get field "+thisJoinPointStaticPart);
 }

 before() : set(* C.*)  {
	System.err.print(":set field "+thisJoinPointStaticPart);
 }

 public void C.foo() {
	n++;
 }

}

class C {
 int m = 20;
}

public class Oxford {

 public static void main(String[] args) {
	C c = new C();
	c.foo();
 }

}