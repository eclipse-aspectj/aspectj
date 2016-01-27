import java.io.*;

public class Code {
  public static void main(String []argv) {
  }
}

class B<T extends SomeClass & SomeInterface> extends C<T> implements Serializable {
}

class C<T> {}

class SomeClass {}
interface SomeInterface {}
interface SomeOtherInterface {}

class D<T extends SomeInterface&SomeOtherInterface> {}
