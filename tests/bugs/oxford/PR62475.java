/* 
Intertype field initialisers should be resolved in the aspect
(lexical scope), for consistency with intertype method and 
constructor bodies.

The program below compiles without warning, however, binding z
to the z field of the target class.
*/



aspect Aspect {

 public int A.x = z; // CE L14 error: z not visible.

}

class A {
 int z = 0;
}

public class PR62475 {

 public static void main(String[] args) {
System.out.println(new A().x);
 }

}