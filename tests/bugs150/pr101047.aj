aspect Test {
	before() : ( execution(* Foo.foo(..) ) ) {
		System.out.println("before");
		
	}
}

class Foo {
    private String myString = "A String";

    public static void main(String[] args) {
        new Foo().foo();  
    }
    
    private void foo() { 
        String myLocal = myString;
    
        if (myLocal.endsWith("X")) {
      	   String local1 = "local1";
           System.out.println(local1);
        } else if (myLocal.endsWith("Y")) {
           String local2 = "local2";
           System.out.println(local2);
        } else {
      	  String local1 = "local3";
          System.out.println(local1);
        }
      }
}

