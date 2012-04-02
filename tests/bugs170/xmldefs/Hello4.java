import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.*;
public class Hello4 {

	public static int field1;
	public static int field2;

        public static void main(String[] args) {
                printAnnos("field1");
                printAnnos("field2");
        } 

        public static void printAnnos(String fieldname) {
        	try {
	        	Field m = Hello4.class.getDeclaredField(fieldname);
	        	Annotation[] annos = m.getAnnotations();
	        	System.out.println("Annotations on "+fieldname+"? "+(annos!=null && annos.length!=0));
	        	if (annos!=null && annos.length>0) {
		        	List<Annotation> la = new ArrayList<Annotation>();
		        	for (Annotation anno: annos) {
		        		la.add(anno);
		        	}
		        	Collections.<Annotation>sort(la,new AnnoComparator());
		        	
	        		System.out.println("Annotation count is "+annos.length);
	        		for (Annotation anno: la) {
	        			System.out.println(anno);
	        		}
	        	}
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
        static class AnnoComparator implements Comparator<Annotation> {
        	public int compare(Annotation a, Annotation b) {
        		return a.toString().compareTo(b.toString());
        	}
        }
}
