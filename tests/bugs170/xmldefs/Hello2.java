import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class Hello2 {

	public static int i;

        public static void main(String[] args) {
                printAnnos("i");
        } 

        public static void printAnnos(String fieldname) {
        	try {
	        	Field m = Hello2.class.getDeclaredField(fieldname);
	        	Annotation[] annos = m.getAnnotations();
	        	System.out.println("Annotations on "+fieldname+"? "+(annos!=null && annos.length!=0));
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
