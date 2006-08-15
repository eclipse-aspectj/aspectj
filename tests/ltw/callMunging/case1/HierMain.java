import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import org.aspectj.weaver.loadtime.WeavingURLClassLoader;

public class HierMain {

  public static void main(String []argv) {
	  try {
		  System.out.println("into:main");
		  ClassLoader baseLoader = HierMain.class.getClassLoader(); 
		  URL base = baseLoader.getResource("HierMain.class");
		  String urlstr = base.toExternalForm();
		  int idx = urlstr.indexOf("classes.jar!");
		  String sub = urlstr.substring("jar:".length(), idx)+"/sub.hiddenjar";
		  URL subUrls[] = new URL[] { new URL(sub) };
		  WeavingURLClassLoader loader = new WeavingURLClassLoader(subUrls, baseLoader);
		  Class clazzA = Class.forName("A", false, loader);
		  Method clazzAMethod = clazzA.getMethod("method",null);
		  clazzAMethod.invoke(clazzA.newInstance(),null);
		  System.out.println("leave:main");
	  } catch (Throwable t) {
		  t.printStackTrace();
	  }
  }
  
}
