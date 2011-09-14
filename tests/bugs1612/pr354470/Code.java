import org.aspectj.lang.annotation.*;
import java.lang.annotation.*;
import java.lang.reflect.*;

@Aspect("perthis(transactional())")
public class Code {
   @Pointcut("execution(@Transactional * * (..))")
   public void transactional() { }
   
   public static void main(String[] args) {
	   print(AAA.class);
	   print(BBB.class);
	   print(CCC.class);
   }
   
   public static void print(Class clazz) {
	   System.out.println(clazz.getName());
	   Class[] ifs = clazz.getInterfaces();
	   if (ifs!=null) {
		   for (int i=0;i<ifs.length;i++) {
			   System.out.println(ifs[i]);
		   }
	   }
	   Field[] fs = clazz.getDeclaredFields();
	   if (fs!=null) {
		   for (int i=0;i<fs.length;i++) {
			   System.out.println(fs[i]);
		   }
	   }
   }
}

class AAA { 
  public void m() { }
}

class BBB { 
  public void m() { }
}

class CCC {
  @Transactional
  public void m() { }
}


@Retention(RetentionPolicy.RUNTIME) @interface Transactional {}

