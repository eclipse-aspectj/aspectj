import java.io.*;

public class Code {
  public void m() { // throws IOException {
    try (FileReader reader = new FileReader("test.txt")) {
      System.out.println("");
    }
  }
}
aspect X {
  declare soft: IOException: within(*);
}
