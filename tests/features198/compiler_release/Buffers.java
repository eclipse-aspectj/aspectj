import java.nio.Buffer;
import java.nio.ByteBuffer;

public class Buffers {
  /**
   * Running this method will fail during runtime on JDK 8, if compiled on JDK 9+ with {@code -source 8 -target 8},
   * because the API has changed: In JDK 8 there was only {@code Buffer.flip()}, but since JDK 9 it is overloaded by
   * {@code ByteBuffer.flip()}.
   * <p>
   * Therefore, it is imperative to compile against the old API, using the correct boot classpath. On JDK 9+, the
   * canonical way to do this is to use {@code --release 8}, because the JDK contains a compatibility layer exactly for
   * this purpose.
   * <p>
   * If incorrectly compiled against JDK 9+ API, this will fail with:
   * <pre>{@code java.lang.NoSuchMethodError: java.nio.ByteBuffer.flip()Ljava/nio/ByteBuffer; }</pre>
   */
  public static Buffer flip(ByteBuffer buffer) {
    return buffer.flip();
  }
}
