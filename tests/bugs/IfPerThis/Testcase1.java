public class Testcase1 {

  public static void main(String [] args) {
	new Testcase1().sayhi();
  }

  public void sayhi() {
  	System.out.println("Hello World");
  }

}

// Note the use of an if inside a perthis, causes the ajc compiler to
//	  throw an exception
aspect Aspect perthis(if(4==3)) {
		
	before () : call(* println(..)) && !within(Aspect*) { 
		System.out.println("Advice 1");
    }

}

aspect Aspect2 pertarget(if(3==4)) {}

aspect Aspect3 percflow(if(3==4)) {}

aspect Aspect4 percflowbelow(if(3==4)) {}