import org.aspectj.testing.*;

public class PR415 {
    public static void main(String[] args) {
        ASTObject ast = new ASTObject();
        ast.f1();
        ast.f2();
        ast.f3();
        ast.g1();
        ast.g2();
        ast.g3();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEventsInString("V(V),V(S),V(SS),f1,f2,f3");
        Tester.expectEventsInString("I(V),I(S),I(SS),g1,g2,g3");
        Tester.expectEventsInString("Vc,VcS,VcSS,Ic,IcS,IcSS");
    }
}

aspect Loses {
    void around(ASTObject ast): 
	  call(void ASTObject.voidMethod()) && target(ast) {
        Tester.event("Vc");				
        proceed(ast);					
    }							
    void around(ASTObject ast, String msg): 
	  call(void ASTObject.voidMethod(String)) && target(ast) && args(msg) {
        Tester.event("VcS");				
        proceed(ast,msg);				
    }							
    void around(ASTObject ast, String msg1, String msg2): 
	  call(void ASTObject.voidMethod(String, String)) && target(ast) && args(msg1, msg2) {
        Tester.event("VcSS");
        proceed(ast,msg1,msg2);
    }

    int around(ASTObject ast): 
	call(int ASTObject.intMethod()) && target(ast) {
        Tester.event("Ic");			       
        return proceed(ast);			       
    }						       
    int around(ASTObject ast, String msg):  
  	  call(int ASTObject.intMethod(String)) && target(ast) && args(msg) {
        Tester.event("IcS");        		       
        return proceed(ast,msg);		       
    }						       
    int around(ASTObject ast, String msg1, String msg2):
 	  call(int ASTObject.intMethod(String, String)) && target(ast) && args(msg1, msg2) {
        Tester.event("IcSS");        
        return proceed(ast,msg1,msg2);
    }    
}

class ASTObject {
    void voidMethod() { Tester.event("V(V)"); }
    void voidMethod(String msg) { Tester.event("V(S)"); }
    void voidMethod(String msg1, String msg2) { Tester.event("V(SS)"); }
    void f1() { voidMethod(); Tester.event("f1"); }
    void f2() { voidMethod(null); Tester.event("f2"); }
    void f3() { voidMethod(null, null); Tester.event("f3"); }

    int intMethod() { Tester.event("I(V)"); return -1; }
    int intMethod(String msg) { Tester.event("I(S)"); return -1; }
    int intMethod(String msg1, String msg2) { Tester.event("I(SS)"); return -1; }
    void g1() { intMethod(); Tester.event("g1"); }
    void g2() { intMethod(null); Tester.event("g2"); }
    void g3() { intMethod(null, null); Tester.event("g3"); }    
}
