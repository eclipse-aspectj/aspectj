/**
 * Example according to <a href="https://openjdk.org/jeps/445">JEP 445</a>
 * <p>
 * This actually does not require any compiler changes and should be compilable without {@code --enable-preview}.
 * It merely serves as a test case for the JVM launcher protocol.
 */
public class NamedClassWithSimpleMainMethodPreview1 {
  void main() {
    System.out.println("Hello world!");
  }
}
