import org.aspectj.testing.Tester;
import org.aspectj.lang.*;
import java.io.*;


/** @testcase Bugzilla Bug 29691  
   Static inner aspects cannot reference user defined pointcuts 
 
 */
public class SoftWithin {

    static void foo() throws IOException {
        throw new IOException();
    }

    public static void main(String[] args) throws Exception{
    	try {
            foo();
    	} catch (SoftException se) {
    		return;
    	}    
    	Tester.checkFailed("should have got SoftException");
    }
}


aspect Soften {

    declare soft : IOException : within(SoftWithin);
}



