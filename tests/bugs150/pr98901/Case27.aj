// "public annotated ITD field"

import java.lang.annotation.*;
import java.lang.reflect.Field;

@Retention(RetentionPolicy.RUNTIME)
@interface anInterface{}

class A27{
}

aspect B27 {
	
	@anInterface
	public int A27.a;
	
	public static void main(String [] args){
		Class c = A27.class;
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
