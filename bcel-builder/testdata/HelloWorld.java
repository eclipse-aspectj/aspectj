import java.io.*;

public class HelloWorld {
      public static void main(String[] argv) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String name = null;

        try {
	      System.out.print("Please enter your name> ");
	      name = in.readLine();
        } catch(IOException e) { return; }
          System.out.println("Hello, " + name);
      }
    }
