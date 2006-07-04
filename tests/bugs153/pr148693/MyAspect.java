package mypackage; 

public aspect MyAspect { 

    pointcut executeMethod(): within(TestClass) && execution(* *(..)); 

    before(): executeMethod() { 
     System.out.println("Enter "+thisJoinPointStaticPart); 
    } 
    after(): executeMethod() { 
     System.out.println("Leave "+thisJoinPointStaticPart); 
    } 
} 

class TestClass { 

public static void main(String[] args) { } 

} 
