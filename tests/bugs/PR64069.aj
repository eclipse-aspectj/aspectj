

aspect Aspect {

    public A.new() {    // CE L5 
       super();
       System.out.println("ITD A()");
    }

    public void A.bar() {  // CE L10
       System.out.println("ITD bar");
   }
    
    public int A.x;  // CE L14

}

class A {

    void foo() {
        A a = new A(); 
        bar(); 
    }

    private int x;
    
    private A() {
        super();
        System.out.println("private A()");
    }

    private void bar() {
        System.out.println("private bar");
    }

}

public class PR64069 { 

    static public void main(String[] args) {
        new A().foo();
    }
}