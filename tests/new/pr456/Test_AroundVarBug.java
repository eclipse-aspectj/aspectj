import java.util.ArrayList;

import org.aspectj.testing.Tester; 

/** @testcase PR#456 advice on advice in usejavac mode */
public class Test_AroundVarBug {
    {
        new ArrayList ().iterator ();
    }
    public static void main (String[] args) {
        new   Test_AroundVarBug(); 
        Tester.checkAllEvents(); 
    } 
    
} 

