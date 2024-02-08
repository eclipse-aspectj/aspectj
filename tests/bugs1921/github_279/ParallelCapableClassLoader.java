import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ParallelCapableClassLoader extends ClassLoader {
  private final ClassLoader delegate;
  private final String classNameToHandle;

  static {
    if (!ClassLoader.registerAsParallelCapable())
      throw new RuntimeException("Failed to register " + ParallelCapableClassLoader.class.getName() + " as parallel-capable");
  }

  public ParallelCapableClassLoader(ClassLoader delegate, String classNameToHandle) {
    this.delegate = delegate;
    this.classNameToHandle = classNameToHandle;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> c = this.findLoadedClass(name);
    if (c == null && name.equals(classNameToHandle)) {
      byte[] bytes = getClassBytes(name);
      try {
        c = defineClass(name, bytes, 0, bytes.length);
      }
      catch (LinkageError e) {
        c = findLoadedClass(name);
        if (c == null)
          throw e;
      }
    }
    if (c == null)
      c = delegate.loadClass(name);
    if (resolve)
      this.resolveClass(c);
    return c;
  }

  private byte[] getClassBytes(String name) {
    String classFilePath = name.replace('.', File.separatorChar) + ".class";
    try (InputStream inputStream = delegate.getResourceAsStream(classFilePath)) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      int bytesRead;
      byte[] buffer = new byte[4096];
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      return outputStream.toByteArray();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
