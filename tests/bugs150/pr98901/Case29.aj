// "public annotated ITD-on-itself field"

import java.lang.annotation.*;
import java.lang.reflect.Field;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

aspect B29 {
	
	@anInterface
	public int B29.a;
	
	public static void main(String [] args){
		Class c = B29.class;
		try {
			Field m = c.getDeclaredField("a");
			Annotation [] anns = m.getDeclaredAnnotations();
			for (int i = 0;i < anns.length;i++){
				System.out.println(anns[i]);
			}
		} catch (Exception e){
			System.out.println("exceptional!");
		}
	}
}
