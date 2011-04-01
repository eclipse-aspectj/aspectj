package mypackage;



aspect Azpect {

    pointcut pc(Object o) : this(o) && execution(* (Code).n*(..));

    after(Object o) throwing(Exception e) : pc(o) {
    	System.out.println("caught it");
//    	e.printStackTrace();
    }
    
}

public class Code {

	// anotherCaughtMethod is NOT advised -- <<< ERROR <<< this should be advised
    public void n() { throw new RuntimeException("n"); }
    
    public static void main(String[]argv) {
    	try {
    		new Code().n();
    	} catch (Exception e) {
    		
    	}
		System.out.println("done");
    }

}
