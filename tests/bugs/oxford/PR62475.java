
aspect Aspect {

 public int A.x = z; // okay, z is visible.
 
}

class A {
 int z = 0;
}

public class PR62475 {

 public static void main(String[] args) {
System.out.println(new A().x);
 }

}