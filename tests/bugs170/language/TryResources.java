public class TryResources {
}

aspect Foo {
  before(): execution(* *(..)) {
try (
   InputStream in = new FileInputStream(src);
   OutputStream out = new FileOutputStream(dest))
{
 // code
}

    
  }
}
