
package app;

public class MainExecStartLines {

    public static void main(String[] args) // 6
    { // 7

        
        
        
        String t = "....".substring(0);        // 12
    }
    void go(String s) {
        try {
            String t = "....".substring(0);
        } catch (RuntimeException e) {   // 17


            String t = "....".substring(0);       // 20
        }
    }
}
/*
 * known limitation: 
 * For static shadow of [method|handler] execution join points 
 * in binary form, only can detect first line of code.
 */
