package mypackage;

aspect Azpect {

    pointcut pc(Object o) : this(o) && execution(* Code2.n*(..) throws Exception+);

    after(Object o) throwing(Exception e) : pc(o) {
    	System.out.println("caught it: "+thisJoinPointStaticPart);
    }
    
}

public class Code2 {


    public void n1() { throw new RuntimeException("n"); }
    public void n2() throws MyException { throw new MyException(); }
    
    public static void main(String[]argv) {
    	try {
    		new Code2().n1();
    	} catch (Exception e) {}
    	try {
    		new Code2().n2();
    	} catch (Exception e) {}
		System.out.println("done");
    }

}

class MyException extends Exception {
	
	
}