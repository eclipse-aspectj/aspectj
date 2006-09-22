public class SampleTest {
    public interface ByteReadingStrategy {
        void readBytes(java.io.InputStream str);
    }

    public ByteReadingStrategy byteReadingStrategy;

    private final ByteReadingStrategy offsetBuf = new ByteReadingStrategy() {
        public void readBytes(java.io.InputStream str) {
            str.read();
        }
    };

   private class NamedByteReadingStrategy {
        public void readBytes(java.io.InputStream str) {
            str.read();
        }
    };

    public void foo(){}
}

aspect Soften {
    pointcut softenedTests() : 
        within(SampleTest+) && execution(* *(..)) && !execution(* *(..) throws Exception+);

    declare soft: Exception+: softenedTests();
}
