/* intertype method declarations should be a static context;
it is incorrect to refer to instance variables of the
originating aspect. ajc fails to check this properly when
an ITD method is applied to an inner class.

The example below compiles fine, but then throws

java.lang.NoSuchFieldError: zzz

when run.

*/

aspect NewFoo { 

 int zzz = 3;

 public void Aaa.Ccc.bar() {
    System.out.println(zzz); // CE L19: illegal reference to zzz
 }
}

class Aaa {

 public class Ccc {
    
 }

 public Ccc ccc;

 public Aaa() {
    ccc = new Ccc();
 }
}

class IllegalRef { 


 public static void main(String[] args) { 
    Aaa aaa = new Aaa();
    aaa.ccc.bar();
 } 

}