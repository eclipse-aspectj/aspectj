import java.io.*;

public class TryResourcesAspect {
}

aspect Foo {
  before(): execution(* *(..)) {
    String src = "foo.txt";
    String dest = "foocopy.txt";
    try (
 //     InputStream in = new FileInputStream(src);
//      OutputStream out = new FileOutputStream(dest))
MyCustomInputStream is = new MyCustomInputStream(src))
      {
         // code
      }
  }


  static class MyCustomInputStream implements Closeable {
     MyCustomInputStream(String src) {}
     public void close() throws IOException {
     }
  }
}
