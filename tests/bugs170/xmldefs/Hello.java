import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class Hello {

        public static void main(String[] args) {
                sayHello();
                printAnnos("sayHello");
        } 

        public static void sayHello() {
                System.out.println("Hello");
                sayWorld();
        } 

        public static int sayWorld() {
                System.out.println("World");
                return 0;
        }

        public static void printAnnos(String methodname) {
        	try {
	        	Method m = Hello.class.getDeclaredMethod(methodname);
	        	Annotation[] annos = m.getAnnotations();
	        	System.out.println("Annotations on "+methodname+"? "+(annos!=null && annos.length!=0));
	        	if (annos!=null && annos.length>0) {
	        		System.out.println("Annotation count is "+annos.length);
	        		for (Annotation anno: annos) {
	        			System.out.println(anno);
	        		}
	        	}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
}
