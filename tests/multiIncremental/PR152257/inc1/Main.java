package test;

import java.io.IOException;

public class Main {
        public void testMethod() throws IOException {
                methodThatThrows();
        }
 
        public static void methodThatThrows() throws IOException {
                System.out.println("Inside method that may throw an IOException");
        }
}