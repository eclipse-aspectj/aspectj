package a;

public aspect X pertypewithin(p..*) {
	
 int i = 0;

 public int getI() { return i; }
 
 public void setI(int i) { this.i = i;}
 
 after() returning: execution(* sayhi(..)) {
   System.err.println("after() returning from a method call to sayhi()");
   i++;
 }
 
 after() returning: execution(* main(..)) {
 	System.err.println("callcount = "+i);
 }

 public static void main(String []argv) {
   System.err.println("X.main() running");
 }
}
